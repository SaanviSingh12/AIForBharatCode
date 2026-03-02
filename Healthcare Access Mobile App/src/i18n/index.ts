// i18n - Internationalization
// Translation files for Sahayak Healthcare App

import { bn } from './locales/bn';
import { en } from './locales/en';
import { gu } from './locales/gu';
import { hi } from './locales/hi';
import { kn } from './locales/kn';
import { ml } from './locales/ml';
import { mr } from './locales/mr';
import { pa } from './locales/pa';
import { ta } from './locales/ta';
import { te } from './locales/te';

export const translations = {
    en,
    hi,
    ta,
    te,
    bn,
    mr,
    gu,
    kn,
    ml,
    pa,
} as const;

export type LanguageCode = keyof typeof translations;
export type TranslationKey = keyof typeof en;

/**
 * Get translations for a specific language
 * Falls back to English if language not found
 */
export const getTranslations = (languageCode: string) => {
    return translations[languageCode as LanguageCode] || translations.en;
};

/**
 * Language configuration with voice support indicators
 */
export const languageConfig = {
    en: {
        code: 'en',
        name: 'English',
        nativeName: 'English',
        voiceInput: true,   // AWS Transcribe supported
        voiceOutput: true,  // AWS Polly supported
        direction: 'ltr',
    },
    hi: {
        code: 'hi',
        name: 'Hindi',
        nativeName: 'हिंदी',
        voiceInput: true,   // AWS Transcribe supported
        voiceOutput: true,  // AWS Polly supported
        direction: 'ltr',
    },
    ta: {
        code: 'ta',
        name: 'Tamil',
        nativeName: 'தமிழ்',
        voiceInput: true,   // AWS Transcribe supported
        voiceOutput: false, // Limited Polly support
        direction: 'ltr',
    },
    te: {
        code: 'te',
        name: 'Telugu',
        nativeName: 'తెలుగు',
        voiceInput: true,   // AWS Transcribe supported
        voiceOutput: false, // Limited Polly support
        direction: 'ltr',
    },
    bn: {
        code: 'bn',
        name: 'Bengali',
        nativeName: 'বাংলা',
        voiceInput: false,  // NOT supported by AWS Transcribe
        voiceOutput: false, // NOT supported by AWS Polly
        direction: 'ltr',
    },
    mr: {
        code: 'mr',
        name: 'Marathi',
        nativeName: 'मराठी',
        voiceInput: true,   // AWS Transcribe supported
        voiceOutput: false, // Limited Polly support
        direction: 'ltr',
    },
    gu: {
        code: 'gu',
        name: 'Gujarati',
        nativeName: 'ગુજરાતી',
        voiceInput: false,  // NOT supported by AWS Transcribe
        voiceOutput: false, // NOT supported by AWS Polly
        direction: 'ltr',
    },
    kn: {
        code: 'kn',
        name: 'Kannada',
        nativeName: 'ಕನ್ನಡ',
        voiceInput: true,   // AWS Transcribe supported
        voiceOutput: false, // Limited Polly support
        direction: 'ltr',
    },
    ml: {
        code: 'ml',
        name: 'Malayalam',
        nativeName: 'മലയാളം',
        voiceInput: false,  // NOT supported by AWS Transcribe
        voiceOutput: false, // NOT supported by AWS Polly
        direction: 'ltr',
    },
    pa: {
        code: 'pa',
        name: 'Punjabi',
        nativeName: 'ਪੰਜਾਬੀ',
        voiceInput: false,  // NOT supported by AWS Transcribe
        voiceOutput: false, // NOT supported by AWS Polly
        direction: 'ltr',
    },
} as const;

export const languages = Object.values(languageConfig);

/**
 * Check if voice input is supported for a language
 */
export const isVoiceInputSupported = (languageCode: string): boolean => {
    return languageConfig[languageCode as LanguageCode]?.voiceInput ?? false;
};

/**
 * Check if voice output is supported for a language
 */
export const isVoiceOutputSupported = (languageCode: string): boolean => {
    return languageConfig[languageCode as LanguageCode]?.voiceOutput ?? false;
};
