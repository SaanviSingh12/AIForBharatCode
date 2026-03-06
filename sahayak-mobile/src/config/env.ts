import Constants from "expo-constants";

// Environment configuration for Sahayak mobile app
// These values can be overridden in app.json or app.config.js

const ENV = {
  // Backend API base URL
  API_BASE_URL:
    Constants.expoConfig?.extra?.apiBaseUrl || "http://localhost:8080",

  // API version prefix
  API_VERSION: "/api/v1",

  // Feature flags
  USE_MOCK_DATA: Constants.expoConfig?.extra?.useMockData ?? true,

  // Supported languages (matching backend)
  SUPPORTED_LANGUAGES: [
    "hi-IN",
    "ta-IN",
    "te-IN",
    "kn-IN",
    "mr-IN",
    "bn-IN",
    "gu-IN",
    "ml-IN",
    "pa-IN",
    "en-IN",
  ] as const,

  // Default language
  DEFAULT_LANGUAGE: "hi-IN",

  // Emergency numbers
  EMERGENCY_NUMBER: "112",
  AMBULANCE_NUMBER: "102",
};

// Helper to build full API URLs
export function apiUrl(endpoint: string): string {
  return `${ENV.API_BASE_URL}${ENV.API_VERSION}${endpoint}`;
}

// API endpoints
export const API_ENDPOINTS = {
  TRIAGE: "/triage",
  PRESCRIPTION: "/prescription",
  DOCTORS: "/doctors",
  MEDICINES: "/medicines",
  PHARMACIES: "/pharmacies",
} as const;

export default ENV;
