# i18n - Internationalization

This directory contains all translation files for the Sahayak Healthcare App.

## Structure

```
i18n/
├── index.ts              # Main export file with configuration
└── locales/
    ├── en.ts            # English (base language)
    ├── hi.ts            # Hindi हिंदी
    ├── ta.ts            # Tamil தமிழ்
    ├── te.ts            # Telugu తెలుగు
    ├── bn.ts            # Bengali বাংলা
    ├── mr.ts            # Marathi मराठी
    ├── gu.ts            # Gujarati ગુજરાતી
    ├── kn.ts            # Kannada ಕನ್ನಡ
    ├── ml.ts            # Malayalam മലയാളം
    └── pa.ts            # Punjabi ਪੰਜਾਬੀ
```

## Language Support Status

### ✅ Full Support (Voice Input + Voice Output)
- **English (en)** - AWS Transcribe + Polly
- **Hindi (hi)** - AWS Transcribe + Polly

### ⚠️ Partial Support (Voice Input Only)
- **Tamil (ta)** - AWS Transcribe ✓, Polly limited
- **Telugu (te)** - AWS Transcribe ✓, Polly limited
- **Kannada (kn)** - AWS Transcribe ✓, Polly limited
- **Marathi (mr)** - AWS Transcribe ✓, Polly limited

### ❌ Text Only (No Voice Support)
- **Bengali (bn)** - No AWS support
- **Gujarati (gu)** - No AWS support
- **Malayalam (ml)** - No AWS support
- **Punjabi (pa)** - No AWS support

## Usage

### In Components

```tsx
import { getTranslations } from '../../i18n';
import { useApp } from '../context/AppContext';

const MyComponent = () => {
  const { language } = useApp();
  const t = getTranslations(language);
  
  return (
    <div>
      <h1>{t.home}</h1>
      <button>{t.submit}</button>
    </div>
  );
};
```

### Check Voice Support

```tsx
import { isVoiceInputSupported, isVoiceOutputSupported } from '../../i18n';

const canRecord = isVoiceInputSupported(language);
const canSpeak = isVoiceOutputSupported(language);
```

### Get Language Config

```tsx
import { languageConfig } from '../../i18n';

const config = languageConfig['hi'];
console.log(config.nativeName); // "हिंदी"
console.log(config.voiceInput); // true
```

## Adding New Translations

### 1. Add New Language File

Create a new file in `locales/` (e.g., `or.ts` for Odia):

```typescript
import { TranslationKeys } from './en';

export const or: TranslationKeys = {
  home: 'ହୋମ',
  symptomEntry: 'ଲକ୍ଷଣ ଯାଞ୍ଚ',
  // ... add all keys from en.ts
};
```

### 2. Update index.ts

```typescript
// Add import
import { or } from './locales/or';

// Add to translations object
export const translations = {
  en, hi, ta, te, bn, mr, gu, kn, ml, pa,
  or, // Add new language
} as const;

// Add to languageConfig
export const languageConfig = {
  // ... existing languages
  or: {
    code: 'or',
    name: 'Odia',
    nativeName: 'ଓଡ଼ିଆ',
    voiceInput: false,  // Check AWS Transcribe support
    voiceOutput: false, // Check AWS Polly support
    direction: 'ltr',
  },
};
```

### 3. Type Safety

The `TranslationKeys` type ensures all language files have the same keys as English. TypeScript will show errors if any keys are missing.

## Translation Keys Reference

All language files must include these keys:

### Navigation
- `home`, `symptomEntry`, `findDoctor`, `prescription`, `profile`

### Symptom Entry
- `speakSymptoms`, `typeSymptoms`, `analyzing`, `startRecording`, `recording`

### Doctor Search
- `findDoctors`, `searchDoctors`, `governmentDoctor`, `independentDoctor`, `commercialDoctor`
- `experience`, `years`, `languages`, `viewDetails`

### Prescription
- `uploadPrescription`, `takePicture`, `browsePharmacies`
- `governmentPharmacy`, `commercialPharmacy`, `savings`, `genericAlternative`

### Emergency
- `emergency`, `call`, `emergencyServices`

### Profile
- `changeLanguage`, `settings`, `aboutApp`

### Common
- `loading`, `error`, `retry`, `cancel`, `submit`, `back`, `next`

### Messages
- `noResults`, `tryAgain`, `selectLanguage`

## Backend Language Mapping

The backend uses different language codes for AWS services:

| Frontend | Backend AWS Code |
|----------|-----------------|
| en       | en-IN           |
| hi       | hi-IN           |
| ta       | ta-IN           |
| te       | te-IN           |
| kn       | kn-IN           |
| mr       | mr-IN           |

See `sahayak-backend/src/main/java/com/sahayak/service/TranscribeService.java` for AWS language code mapping.

## Best Practices

1. **Keep translations consistent** - Use the same tone across all languages
2. **Use native speakers** - Have translations reviewed by native speakers
3. **Test on real devices** - Verify text rendering for complex scripts
4. **Consider text length** - Some languages expand significantly (e.g., German)
5. **Use placeholders** - For dynamic content: `{name}`, `{count}`, etc.

## Future Improvements

- [ ] Add pluralization support
- [ ] Add date/time formatting per locale
- [ ] Add number formatting per locale  
- [ ] Add RTL (Right-to-Left) support for Urdu
- [ ] Add gender-specific translations where needed
- [ ] External translation management (e.g., Crowdin, Lokalise)

## Contributing

When adding new UI text:

1. Add the key to `en.ts` first
2. Add to all other language files (or mark as TODO)
3. Run TypeScript check: `npm run build`
4. Test in app with multiple languages

## Resources

- [AWS Transcribe Language Support](https://docs.aws.amazon.com/transcribe/latest/dg/supported-languages.html)
- [AWS Polly Voice List](https://docs.aws.amazon.com/polly/latest/dg/voicelist.html)
- [Unicode CLDR](https://cldr.unicode.org/) - Language data
- [i18next](https://www.i18next.com/) - Advanced i18n library (for future migration)
