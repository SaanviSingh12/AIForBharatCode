import { useApp } from '../context/AppContext';
import type { Translations } from './en';
import { en } from './en';
import { hi } from './hi';
import { bn } from './bn';
import { te } from './te';
import { mr } from './mr';
import { ta } from './ta';
import { gu } from './gu';
import { kn } from './kn';
import { ml } from './ml';
import { pa } from './pa';

export type { Translations };

const allTranslations: Record<string, Translations> = {
  en,
  hi,
  bn,
  te,
  mr,
  ta,
  gu,
  kn,
  ml,
  pa,
};

/**
 * Hook to get translations for the currently selected language.
 * Falls back to English if the selected language is not available.
 */
export function useTranslation(): Translations {
  const { language } = useApp();
  return allTranslations[language] || allTranslations.en;
}

/**
 * Get translations for a specific language code (for use outside React components).
 */
export function getTranslations(langCode: string): Translations {
  return allTranslations[langCode] || allTranslations.en;
}
