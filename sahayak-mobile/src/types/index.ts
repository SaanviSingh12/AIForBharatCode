// ─────────────────────────────────────────────
// Sahayak Mobile - TypeScript Types
// Shared interfaces matching Java DTOs from backend
// ─────────────────────────────────────────────

export interface HospitalDto {
  id: string;
  name: string;
  type: "government" | "private";
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

// Location type for geolocation
export interface LocationCoords {
  lat?: string;
  lng?: string;
}

// User profile type
export interface UserProfile {
  name: string;
  age: string;
  gender: string;
}
