// ─────────────────────────────────────────────
// Sahayak Mobile - API Service
// Connects React Native app to Spring Boot backend
// Uses expo-audio for audio playback (replaces web Audio API)
// ─────────────────────────────────────────────

import { createAudioPlayer, type AudioPlayer } from "expo-audio";
import ENV, { apiUrl, API_ENDPOINTS } from "../config/env";
import type {
  TriageApiResponse,
  PrescriptionApiResponse,
  MedicineDto,
  PharmacyDto,
  DoctorDto,
  LocationCoords,
} from "../types";
import { getAwsLanguageCode } from "../i18n";

// Re-export types for convenience
export type {
  TriageApiResponse,
  PrescriptionApiResponse,
  MedicineDto,
  PharmacyDto,
  DoctorDto,
  HospitalDto,
  LocationCoords,
} from "../types";

// ── Language code mapping ─────────────────────

/**
 * Maps frontend language codes ("hi", "en", "ta")
 * to AWS-compatible BCP-47 codes ("hi-IN", "en-IN", "ta-IN")
 */
export function mapLanguageCode(code: string): string {
  const map: Record<string, string> = {
    en: "en-IN",
    hi: "hi-IN",
    ta: "ta-IN",
    te: "te-IN",
    kn: "kn-IN",
    mr: "mr-IN",
    bn: "bn-IN",
    gu: "gu-IN",
    ml: "ml-IN",
    pa: "pa-IN",
  };
  // Handle both "hi" and "hi-IN" formats
  const shortCode = code?.split("-")[0]?.toLowerCase();
  return map[shortCode] ?? code ?? "hi-IN";
}

// ── Core API calls ────────────────────────────

/**
 * Send audio URI or direct text for symptom triage.
 * Returns AI analysis + nearby hospital list.
 *
 * Note: For React Native, we pass audio file URI instead of Blob
 */
export async function analyzeSymptoms(
  audioUri: string | null,
  language: string,
  location: LocationCoords = {},
  directText?: string
): Promise<TriageApiResponse> {
  const formData = new FormData();

  if (audioUri) {
    // React Native FormData handles file URIs differently than web
    formData.append("audio", {
      uri: audioUri,
      type: "audio/m4a", // iOS records in m4a, Android in various formats
      name: "recording.m4a",
    } as unknown as Blob);
  }

  formData.append("language", mapLanguageCode(language));
  if (location.lat) formData.append("lat", location.lat);
  if (location.lng) formData.append("lng", location.lng);
  if (directText) formData.append("directText", directText);

  const res = await fetch(apiUrl(API_ENDPOINTS.TRIAGE), {
    method: "POST",
    body: formData,
    headers: {
      // Let fetch set Content-Type with boundary for FormData
    },
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Triage API error ${res.status}: ${text}`);
  }

  return res.json();
}

/**
 * Upload prescription image for analysis.
 * Returns extracted medicines + generic alternatives + Jan Aushadhi locations.
 *
 * @param imageUri - Local file URI from expo-image-picker
 */
export async function analyzePrescription(
  imageUri: string,
  language: string,
  location: LocationCoords = {}
): Promise<PrescriptionApiResponse> {
  const formData = new FormData();

  // React Native FormData with file URI
  formData.append("image", {
    uri: imageUri,
    type: "image/jpeg",
    name: "prescription.jpg",
  } as unknown as Blob);

  formData.append("language", mapLanguageCode(language));
  if (location.lat) formData.append("lat", location.lat);
  if (location.lng) formData.append("lng", location.lng);

  const res = await fetch(apiUrl(API_ENDPOINTS.PRESCRIPTION), {
    method: "POST",
    body: formData,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Prescription API error ${res.status}: ${text}`);
  }

  return res.json();
}

/**
 * Analyze prescription from text input (no image needed).
 */
export async function analyzePrescriptionText(
  prescription: string,
  language: string,
  location: LocationCoords = {}
): Promise<PrescriptionApiResponse> {
  const params = new URLSearchParams();
  params.append("prescription", prescription);
  params.append("language", mapLanguageCode(language));
  if (location.lat) params.append("lat", location.lat);
  if (location.lng) params.append("lng", location.lng);

  const res = await fetch(`${apiUrl(API_ENDPOINTS.PRESCRIPTION)}Text`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: params.toString(),
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Prescription text API error ${res.status}: ${text}`);
  }

  return res.json();
}

/**
 * Check if backend is reachable.
 */
export async function checkHealth(): Promise<boolean> {
  try {
    const res = await fetch(`${ENV.API_BASE_URL}/api/v1/health`, {
      method: "GET",
    });
    return res.ok;
  } catch {
    return false;
  }
}

// ── Audio playback using expo-audio ───────────

let currentPlayer: AudioPlayer | null = null;

/**
 * Play base64-encoded MP3 audio returned by Polly TTS.
 * Uses expo-audio's imperative createAudioPlayer API.
 */
export async function playAudioResponse(base64: string): Promise<void> {
  try {
    // Stop & release any currently playing audio
    if (currentPlayer) {
      currentPlayer.remove();
      currentPlayer = null;
    }

    // Create player from base64 data URI
    const player = createAudioPlayer(
      { uri: `data:audio/mp3;base64,${base64}` }
    );

    currentPlayer = player;

    // Clean up when playback finishes
    player.addListener("playbackStatusUpdate", (status) => {
      if (status.didJustFinish) {
        player.remove();
        if (currentPlayer === player) {
          currentPlayer = null;
        }
      }
    });

    player.play();
  } catch (err) {
    console.warn("Could not play audio response:", err);
  }
}

/**
 * Stop any currently playing audio.
 */
export async function stopAudio(): Promise<void> {
  if (currentPlayer) {
    currentPlayer.pause();
    currentPlayer.remove();
    currentPlayer = null;
  }
}

// ── Medicine & Pharmacy endpoints ─────────────

/**
 * Search medicines by name/query.
 */
export async function searchMedicines(
  query: string,
  language: string
): Promise<MedicineDto[]> {
  const params = new URLSearchParams({
    query,
    language: mapLanguageCode(language),
  });
  const res = await fetch(
    `${apiUrl(API_ENDPOINTS.PRESCRIPTION)}/medicineSearch?${params}`
  );

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Medicine search API error ${res.status}: ${text}`);
  }

  return res.json();
}

/**
 * Get nearby Jan Aushadhi Kendras / pharmacies.
 */
export async function getNearbyPharmacies(
  location: LocationCoords = {}
): Promise<PharmacyDto[]> {
  const params = new URLSearchParams();
  if (location.lat) params.append("lat", location.lat);
  if (location.lng) params.append("lng", location.lng);

  const url = params.toString()
    ? `${apiUrl(API_ENDPOINTS.PRESCRIPTION)}/nearbyPharmacies?${params}`
    : `${apiUrl(API_ENDPOINTS.PRESCRIPTION)}/nearbyPharmacies`;

  const res = await fetch(url);

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Nearby pharmacies API error ${res.status}: ${text}`);
  }

  return res.json();
}

// ── Doctor endpoints ──────────────────────────

/**
 * Get all doctors. Optional query/specialty filters.
 */
export async function getDoctors(
  filters: { query?: string; specialty?: string } = {}
): Promise<DoctorDto[]> {
  const params = new URLSearchParams();
  if (filters.query) params.append("query", filters.query);
  if (filters.specialty) params.append("specialty", filters.specialty);

  const url = params.toString()
    ? `${apiUrl(API_ENDPOINTS.DOCTORS)}?${params}`
    : apiUrl(API_ENDPOINTS.DOCTORS);

  const res = await fetch(url);

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Doctors API error ${res.status}: ${text}`);
  }

  return res.json();
}

/**
 * Get a single doctor by ID.
 */
export async function getDoctorById(id: string): Promise<DoctorDto> {
  const res = await fetch(
    `${apiUrl(API_ENDPOINTS.DOCTORS)}/${encodeURIComponent(id)}`
  );

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Doctor detail API error ${res.status}: ${text}`);
  }

  return res.json();
}

/**
 * Get only government (PMJAY-eligible) doctors.
 */
export async function getGovernmentDoctors(): Promise<DoctorDto[]> {
  const res = await fetch(`${apiUrl(API_ENDPOINTS.DOCTORS)}/government`);

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Government doctors API error ${res.status}: ${text}`);
  }

  return res.json();
}

/**
 * Get doctors filtered by specialty.
 */
export async function getDoctorsBySpecialty(
  specialty: string
): Promise<DoctorDto[]> {
  const res = await fetch(
    `${apiUrl(API_ENDPOINTS.DOCTORS)}/specialty/${encodeURIComponent(specialty)}`
  );

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Doctors by specialty API error ${res.status}: ${text}`);
  }

  return res.json();
}

// ── Resilient API wrappers for poor connectivity ──────────────

import { resilientFetch, isOnline, getNetworkErrorMessage } from '../utils/networkUtils';

export interface ResilientApiResult<T> {
  data: T | null;
  error: string | null;
  isOffline: boolean;
  retryCount: number;
}

/**
 * Wrapper that adds retry logic and offline detection to any async API call.
 * Designed for rural areas with intermittent connectivity.
 */
export async function withRetry<T>(
  apiCall: () => Promise<T>,
  options: {
    maxRetries?: number;
    retryDelay?: number;
    timeout?: number;
    onRetry?: (attempt: number) => void;
    language?: string;
  } = {}
): Promise<ResilientApiResult<T>> {
  const {
    maxRetries = 3,
    retryDelay = 1000,
    timeout = 30000,
    onRetry,
    language = 'en-IN',
  } = options;

  // Check connectivity first
  const online = await isOnline();
  if (!online) {
    return {
      data: null,
      error: 'No internet connection. Please check your network.',
      isOffline: true,
      retryCount: 0,
    };
  }

  let lastError: Error | null = null;
  let attempts = 0;

  for (let attempt = 0; attempt <= maxRetries; attempt++) {
    attempts = attempt + 1;

    try {
      // Create timeout wrapper
      const result = await Promise.race([
        apiCall(),
        new Promise<never>((_, reject) =>
          setTimeout(() => reject(new Error('Request timed out')), timeout)
        ),
      ]);

      return {
        data: result,
        error: null,
        isOffline: false,
        retryCount: attempt,
      };
    } catch (error) {
      lastError = error instanceof Error ? error : new Error(String(error));

      // Don't retry on 4xx errors (client errors)
      if (lastError.message.includes('error 4')) {
        break;
      }

      if (attempt < maxRetries) {
        onRetry?.(attempt + 1);
        // Exponential backoff
        const delay = retryDelay * Math.pow(2, attempt);
        await new Promise((resolve) => setTimeout(resolve, delay));
        
        // Check if still online
        const stillOnline = await isOnline();
        if (!stillOnline) {
          return {
            data: null,
            error: 'Lost internet connection.',
            isOffline: true,
            retryCount: attempts,
          };
        }
      }
    }
  }

  return {
    data: null,
    error: lastError ? getNetworkErrorMessage(lastError, language) : 'Unknown error',
    isOffline: false,
    retryCount: attempts,
  };
}

/**
 * Resilient version of analyzeSymptoms with retry logic
 */
export async function analyzeSymptomsResilient(
  audioUri: string | null,
  language: string,
  location: LocationCoords = {},
  directText?: string,
  options?: { onRetry?: (attempt: number) => void }
): Promise<ResilientApiResult<TriageApiResponse>> {
  return withRetry(
    () => analyzeSymptoms(audioUri, language, location, directText),
    { timeout: 60000, language, ...options } // Longer timeout for audio upload
  );
}

/**
 * Resilient version of analyzePrescription with retry logic
 */
export async function analyzePrescriptionResilient(
  imageUri: string,
  language: string,
  location: LocationCoords = {},
  options?: { onRetry?: (attempt: number) => void }
): Promise<ResilientApiResult<PrescriptionApiResponse>> {
  return withRetry(
    () => analyzePrescription(imageUri, language, location),
    { timeout: 90000, language, ...options } // Longer timeout for image upload
  );
}

/**
 * Resilient version of getDoctors with retry logic
 */
export async function getDoctorsResilient(
  filters?: { query?: string; specialty?: string },
  options?: { onRetry?: (attempt: number) => void }
): Promise<ResilientApiResult<DoctorDto[]>> {
  return withRetry(
    () => getDoctors(filters),
    { timeout: 30000, ...options }
  );
}
