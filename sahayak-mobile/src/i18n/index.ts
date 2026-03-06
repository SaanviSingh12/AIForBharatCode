// ─────────────────────────────────────────────
// Sahayak Mobile - Internationalization
// Language configuration and translations
// ─────────────────────────────────────────────

export type LanguageCode =
  | "en"
  | "hi"
  | "ta"
  | "te"
  | "bn"
  | "mr"
  | "gu"
  | "kn"
  | "ml"
  | "pa";

export interface LanguageConfig {
  code: LanguageCode;
  awsCode: string; // BCP-47 code for AWS services
  name: string;
  nativeName: string;
  voiceInput: boolean; // AWS Transcribe support
  voiceOutput: boolean; // AWS Polly support
}

/**
 * Language configuration with AWS voice support indicators
 */
export const languageConfig: Record<LanguageCode, LanguageConfig> = {
  en: {
    code: "en",
    awsCode: "en-IN",
    name: "English",
    nativeName: "English",
    voiceInput: true,
    voiceOutput: true,
  },
  hi: {
    code: "hi",
    awsCode: "hi-IN",
    name: "Hindi",
    nativeName: "हिंदी",
    voiceInput: true,
    voiceOutput: true,
  },
  ta: {
    code: "ta",
    awsCode: "ta-IN",
    name: "Tamil",
    nativeName: "தமிழ்",
    voiceInput: true,
    voiceOutput: false,
  },
  te: {
    code: "te",
    awsCode: "te-IN",
    name: "Telugu",
    nativeName: "తెలుగు",
    voiceInput: true,
    voiceOutput: false,
  },
  bn: {
    code: "bn",
    awsCode: "bn-IN",
    name: "Bengali",
    nativeName: "বাংলা",
    voiceInput: false,
    voiceOutput: false,
  },
  mr: {
    code: "mr",
    awsCode: "mr-IN",
    name: "Marathi",
    nativeName: "मराठी",
    voiceInput: true,
    voiceOutput: false,
  },
  gu: {
    code: "gu",
    awsCode: "gu-IN",
    name: "Gujarati",
    nativeName: "ગુજરાતી",
    voiceInput: false,
    voiceOutput: false,
  },
  kn: {
    code: "kn",
    awsCode: "kn-IN",
    name: "Kannada",
    nativeName: "ಕನ್ನಡ",
    voiceInput: true,
    voiceOutput: false,
  },
  ml: {
    code: "ml",
    awsCode: "ml-IN",
    name: "Malayalam",
    nativeName: "മലയാളം",
    voiceInput: false,
    voiceOutput: false,
  },
  pa: {
    code: "pa",
    awsCode: "pa-IN",
    name: "Punjabi",
    nativeName: "ਪੰਜਾਬੀ",
    voiceInput: false,
    voiceOutput: false,
  },
};

export const languages = Object.values(languageConfig);

/**
 * Check if voice input is supported for a language
 */
export const isVoiceInputSupported = (code: string): boolean => {
  return languageConfig[code as LanguageCode]?.voiceInput ?? false;
};

/**
 * Check if voice output is supported for a language
 */
export const isVoiceOutputSupported = (code: string): boolean => {
  return languageConfig[code as LanguageCode]?.voiceOutput ?? false;
};

/**
 * Get AWS-compatible language code
 */
export const getAwsLanguageCode = (code: string): string => {
  return languageConfig[code as LanguageCode]?.awsCode ?? "hi-IN";
};

// ─────────────────────────────────────────────
// Basic UI Translations
// ─────────────────────────────────────────────

export const translations = {
  en: {
    home: "Home",
    symptomEntry: "Symptom Checker",
    findDoctor: "Find Doctor",
    prescription: "Prescription",
    profile: "Profile",
    speakSymptoms: "Speak Your Symptoms",
    typeSymptoms: "Or type your symptoms here...",
    analyzing: "Analyzing...",
    startRecording: "Tap to Start Recording",
    recording: "Recording... Tap to Stop",
    findDoctors: "Find Doctors",
    searchDoctors: "Search doctors...",
    governmentDoctor: "Government",
    uploadPrescription: "Upload Prescription",
    takePicture: "Take Picture",
    emergency: "Emergency",
    call: "Call",
    loading: "Loading...",
    error: "Error",
    retry: "Retry",
    cancel: "Cancel",
    submit: "Submit",
    back: "Back",
    selectLanguage: "Select Your Language",
    welcome: "Welcome",
    healthcareForAll: "Healthcare for All Indians",
    savings: "Savings",
    genericAlternative: "Generic Alternative",
  },
  hi: {
    home: "होम",
    symptomEntry: "लक्षण जांच",
    findDoctor: "डॉक्टर खोजें",
    prescription: "नुस्खा",
    profile: "प्रोफाइल",
    speakSymptoms: "अपने लक्षण बोलें",
    typeSymptoms: "या यहाँ अपने लक्षण लिखें...",
    analyzing: "विश्लेषण हो रहा है...",
    startRecording: "रिकॉर्डिंग शुरू करने के लिए टैप करें",
    recording: "रिकॉर्डिंग... रोकने के लिए टैप करें",
    findDoctors: "डॉक्टर खोजें",
    searchDoctors: "डॉक्टर खोजें...",
    governmentDoctor: "सरकारी",
    uploadPrescription: "नुस्खा अपलोड करें",
    takePicture: "फोटो लें",
    emergency: "आपातकाल",
    call: "कॉल करें",
    loading: "लोड हो रहा है...",
    error: "त्रुटि",
    retry: "पुनः प्रयास करें",
    cancel: "रद्द करें",
    submit: "जमा करें",
    back: "वापस",
    selectLanguage: "अपनी भाषा चुनें",
    welcome: "स्वागत है",
    healthcareForAll: "सभी भारतीयों के लिए स्वास्थ्य सेवा",
    savings: "बचत",
    genericAlternative: "जेनेरिक विकल्प",
  },
};

export type TranslationKey = keyof (typeof translations)["en"];

/**
 * Get translations for a specific language
 * Falls back to Hindi (primary target) then English
 */
export const getTranslations = (
  languageCode: string
): (typeof translations)["en"] => {
  const code = languageCode.split("-")[0] as LanguageCode;
  return (
    translations[code as keyof typeof translations] ||
    translations.hi ||
    translations.en
  );
};
