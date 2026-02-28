// ─────────────────────────────────────────────
// Sahayak API Service
// Connects the React frontend to the Spring Boot backend
// ─────────────────────────────────────────────

// eslint-disable-next-line @typescript-eslint/no-explicit-any
const API_BASE: string = (import.meta as any).env?.VITE_API_URL || 'http://localhost:8080';

// ── Types matching Java DTOs ──────────────────

export interface HospitalDto {
  id: string;
  name: string;
  type: 'government' | 'private';
  specialist: string;
  distance: number;
  free: boolean;
  fee: number | null;
  phone: string;
  address: string;
  hasEmergency: boolean;
  pmjayStatus: string | null;
}

export interface MedicineDto {
  brandName: string;
  genericName: string;
  dosage: string;
  brandPrice: number;
  genericPrice: number;
  savingsPercent: number;
  savingsAmount: number;
}

export interface PharmacyDto {
  id: string;
  name: string;
  type: string;
  distance: number;
  phone: string;
  address: string;
  timings: string;
}

export interface TriageApiResponse {
  success: boolean;
  symptomText: string;
  specialist: string;
  isEmergency: boolean;
  urgencyLevel: string;
  summary: string;
  responseText: string;
  audioBase64: string | null;
  hospitals: HospitalDto[];
  error: string | null;
}

export interface PrescriptionApiResponse {
  success: boolean;
  extractedText: string;
  medicines: MedicineDto[];
  totalBrandCost: number;
  totalGenericCost: number;
  totalSavingsPercent: number;
  responseText: string;
  audioBase64: string | null;
  janAushadhiLocations: PharmacyDto[];
  error: string | null;
}

// ── Language code mapping ─────────────────────

/**
 * Maps frontend language codes ("hi", "en", "ta") 
 * to AWS-compatible BCP-47 codes ("hi-IN", "en-IN", "ta-IN")
 */
export function mapLanguageCode(code: string): string {
  const map: Record<string, string> = {
    en: 'en-IN',
    hi: 'hi-IN',
    ta: 'ta-IN',
    te: 'te-IN',
    kn: 'kn-IN',
    mr: 'mr-IN',
    bn: 'bn-IN',
    gu: 'gu-IN',
    ml: 'ml-IN',
    pa: 'pa-IN',
  };
  return map[code?.toLowerCase()] ?? 'hi-IN';
}

// ── Core API calls ────────────────────────────

/**
 * Send audio blob or direct text for symptom triage.
 * Returns AI analysis + nearby hospital list.
 */
export async function analyzeSymptoms(
  audioBlob: Blob | null,
  language: string,
  location: { lat?: string; lng?: string } = {},
  directText?: string
): Promise<TriageApiResponse> {
  const formData = new FormData();

  if (audioBlob) {
    formData.append('audio', audioBlob, 'recording.webm');
  }
  formData.append('language', mapLanguageCode(language));
  if (location.lat) formData.append('lat', location.lat);
  if (location.lng) formData.append('lng', location.lng);
  if (directText) formData.append('directText', directText);

  const res = await fetch(`${API_BASE}/api/v1/triage`, {
    method: 'POST',
    body: formData,
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
 */
export async function analyzePrescription(
  imageFile: File,
  language: string,
  location: { lat?: string; lng?: string } = {}
): Promise<PrescriptionApiResponse> {
  const formData = new FormData();
  formData.append('image', imageFile);
  formData.append('language', mapLanguageCode(language));
  if (location.lat) formData.append('lat', location.lat);
  if (location.lng) formData.append('lng', location.lng);

  const res = await fetch(`${API_BASE}/api/v1/prescription`, {
    method: 'POST',
    body: formData,
  });

  if (!res.ok) {
    const text = await res.text();
    throw new Error(`Prescription API error ${res.status}: ${text}`);
  }

  return res.json();
}

/**
 * Check if backend is reachable.
 */
export async function checkHealth(): Promise<boolean> {
  try {
    const res = await fetch(`${API_BASE}/api/v1/health`, { method: 'GET' });
    return res.ok;
  } catch {
    return false;
  }
}

// ── Audio playback ────────────────────────────

/**
 * Play base64-encoded MP3 audio returned by Polly TTS.
 */
export function playAudioResponse(base64: string): void {
  try {
    const audio = new Audio(`data:audio/mp3;base64,${base64}`);
    audio.play().catch((err) => console.warn('Audio play failed:', err));
  } catch (err) {
    console.warn('Could not play audio response:', err);
  }
}
