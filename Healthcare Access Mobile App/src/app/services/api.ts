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

export interface DoctorDto {
    id: string;
    name: string;
    specialty: string;
    type: string; // "government", "independent", "commercial"
    distance: number;
    available: boolean;
    fee: number;
    phone: string;
    address: string;
    pmjay: boolean;
    rating: number;
    waitTime: string;
    experience: number;
    languages: string[];
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

    const data = await res.json();
    // Normalize: Lombok + Jackson may serialize `boolean isEmergency` as "emergency"
    if (data.isEmergency === undefined && data.emergency !== undefined) {
        data.isEmergency = data.emergency;
    }
    return data;
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

// Track currently playing audio to prevent overlapping playback
let currentAudio: HTMLAudioElement | null = null;

/**
 * Play base64-encoded MP3 audio returned by Polly TTS.
 * Prevents multiple simultaneous playbacks - waits for current audio to finish.
 */
export function playAudioResponse(base64: string): void {
    try {
        // If audio is already playing, stop it first
        if (currentAudio && !currentAudio.paused) {
            currentAudio.pause();
            currentAudio.currentTime = 0;
        }

        // Create and play new audio
        const audio = new Audio(`data:audio/mp3;base64,${base64}`);
        currentAudio = audio;

        // Clear reference when audio ends or errors
        audio.addEventListener('ended', () => {
            currentAudio = null;
        });
        audio.addEventListener('error', () => {
            currentAudio = null;
        });

        audio.play().catch((err) => {
            console.warn('Audio play failed:', err);
            currentAudio = null;
        });
    } catch (err) {
        console.warn('Could not play audio response:', err);
        currentAudio = null;
    }
}

// ── Prescription text & search ────────────────

/**
 * POST /api/v1/prescriptionText
 * Analyze prescription from text input (no image needed).
 */
export async function analyzePrescriptionText(
    prescription: string,
    language: string,
    location: { lat?: string; lng?: string } = {}
): Promise<PrescriptionApiResponse> {
    const params = new URLSearchParams();
    params.append('prescription', prescription);
    params.append('language', mapLanguageCode(language));
    if (location.lat) params.append('lat', location.lat);
    if (location.lng) params.append('lng', location.lng);

    const res = await fetch(`${API_BASE}/api/v1/prescriptionText`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
        body: params,
    });

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`Prescription text API error ${res.status}: ${text}`);
    }

    return res.json();
}

/**
 * GET /api/v1/prescription/medicineSearch
 * Search medicines by name/query.
 */
export async function searchMedicines(
    query: string,
    language: string
): Promise<MedicineDto[]> {
    const params = new URLSearchParams({ query, language: mapLanguageCode(language) });
    const res = await fetch(`${API_BASE}/api/v1/prescription/medicineSearch?${params}`);

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`Medicine search API error ${res.status}: ${text}`);
    }

    return res.json();
}

/**
 * GET /api/v1/prescription/nearbyPharmacies
 * Get nearby Jan Aushadhi Kendras / pharmacies.
 */
export async function getNearbyPharmacies(
    location: { lat?: string; lng?: string } = {}
): Promise<PharmacyDto[]> {
    const params = new URLSearchParams();
    if (location.lat) params.append('lat', location.lat);
    if (location.lng) params.append('lng', location.lng);

    const url = params.toString()
        ? `${API_BASE}/api/v1/prescription/nearbyPharmacies?${params}`
        : `${API_BASE}/api/v1/prescription/nearbyPharmacies`;

    const res = await fetch(url);

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`Nearby pharmacies API error ${res.status}: ${text}`);
    }

    return res.json();
}

// ── Doctor endpoints ──────────────────────────

/**
 * GET /api/v1/doctors
 * Get all doctors. Optional query/specialty filters.
 */
export async function getDoctors(
    filters: { query?: string; specialty?: string } = {},
    location: { lat?: string; lng?: string } = {}
): Promise<DoctorDto[]> {
    const params = new URLSearchParams();
    if (filters.query) params.append('query', filters.query);
    if (filters.specialty) params.append('specialty', filters.specialty);
    if (location.lat) params.append('lat', location.lat);
    if (location.lng) params.append('lng', location.lng);

    const url = params.toString()
        ? `${API_BASE}/api/v1/doctors?${params}`
        : `${API_BASE}/api/v1/doctors`;

    const res = await fetch(url);

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`Doctors API error ${res.status}: ${text}`);
    }

    return res.json();
}

/**
 * GET /api/v1/doctors/:id
 * Get a single doctor by ID.
 */
export async function getDoctorById(id: string): Promise<DoctorDto> {
    const res = await fetch(`${API_BASE}/api/v1/doctors/${encodeURIComponent(id)}`);

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`Doctor detail API error ${res.status}: ${text}`);
    }

    return res.json();
}

/**
 * GET /api/v1/doctors/government
 * Get only government (PMJAY-eligible) doctors.
 */
export async function getGovernmentDoctors(): Promise<DoctorDto[]> {
    const res = await fetch(`${API_BASE}/api/v1/doctors/government`);

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`Government doctors API error ${res.status}: ${text}`);
    }

    return res.json();
}

/**
 * GET /api/v1/doctors/specialty/:specialty
 * Get doctors filtered by specialty.
 */
export async function getDoctorsBySpecialty(specialty: string): Promise<DoctorDto[]> {
    const res = await fetch(`${API_BASE}/api/v1/doctors/specialty/${encodeURIComponent(specialty)}`);

    if (!res.ok) {
        const text = await res.text();
        throw new Error(`Doctors by specialty API error ${res.status}: ${text}`);
    }

    return res.json();
}
