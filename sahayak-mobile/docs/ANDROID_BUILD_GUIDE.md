# Android Build & Testing Guide

## Prerequisites

1. **EAS CLI**: Install globally
   ```bash
   npm install -g eas-cli
   ```

2. **Expo Account**: Required for EAS Build
   ```bash
   eas login
   ```

3. **Project Setup**: Link to Expo project
   ```bash
   cd sahayak-mobile
   eas build:configure
   ```

## Build Profiles

Three profiles configured in `eas.json`:

| Profile | Output | Use Case |
|---------|--------|----------|
| `development` | APK (debug) | Local dev/debugging |
| `preview` | APK | Internal testing, QA |
| `production` | AAB | Play Store release |

## Build Commands

### Development APK (with debug tools)
```bash
eas build --platform android --profile development
```

### Preview APK (for testing)
```bash
eas build --platform android --profile preview
```

### Production AAB (Play Store)
```bash
eas build --platform android --profile production
```

### Local Build (no EAS servers)
```bash
eas build --platform android --profile preview --local
```

## Testing on Physical Device

### 1. Enable Developer Options
- Settings → About Phone → Tap "Build Number" 7 times
- Settings → Developer Options → Enable USB Debugging

### 2. Install APK
```bash
# Download APK from EAS build URL, then:
adb install sahayak-mobile.apk
```

### 3. Run in Expo Go (Development)
```bash
cd sahayak-mobile
npx expo start

# Scan QR code with Expo Go app on Android device
```

## Network Simulation for Rural Testing

### Chrome DevTools (Expo Go)
1. Run app in Expo Go
2. Open Chrome → `chrome://inspect`
3. Click "Configure" and add device IP
4. Use Network tab → Throttling → "Slow 3G"

### Android Emulator Network Throttling
```bash
# Start emulator with network delay
emulator -avd <avd_name> -netdelay 3g -netspeed 3g
```

### Real Device - Developer Options
1. Settings → Developer Options
2. Select Network → "Mobile network settings"
3. Set to 2G/3G only

### Testing Offline Mode
1. Enable Airplane Mode
2. App should show "No internet connection" banner
3. Verify offline messages appear in user's language
4. Re-enable network → Banner should disappear
5. API calls should retry automatically

## Connectivity Testing Checklist

### Offline Handling ✅
- [ ] Offline banner appears when network lost
- [ ] Banner shows in correct language (Hindi/Tamil/etc.)
- [ ] Retry button refreshes network status
- [ ] API calls fail gracefully with user-friendly message

### Slow Network (3G/2G) ✅
- [ ] Warning banner shows for slow connections
- [ ] Loading skeletons display during API calls
- [ ] API calls have 30-60 second timeouts
- [ ] Retry logic works (3 attempts with backoff)

### Network Recovery ✅
- [ ] App resumes normally when network restored
- [ ] Pending operations complete
- [ ] No duplicate submissions

## Permissions Test

Verify these work correctly:

| Permission | Feature | Test |
|------------|---------|------|
| RECORD_AUDIO | Voice symptoms | Record symptom description |
| CAMERA | Prescription photo | Take prescription photo |
| READ_EXTERNAL_STORAGE | Gallery access | Pick prescription from gallery |
| CALL_PHONE | Emergency calls | Tap hospital phone number |
| VIBRATE | Haptic feedback | Tap buttons |

## Backend Connection

### Local Development
```bash
# Terminal 1: Backend
cd sahayak-backend && ./mvnw spring-boot:run

# Terminal 2: Get local IP
ifconfig | grep "inet " | grep -v 127.0.0.1

# Update API_BASE_URL in env.ts with your IP:
# API_BASE_URL: 'http://192.168.x.x:8080'

# Terminal 3: Mobile app
cd sahayak-mobile && npx expo start
```

### Production API
Set in `eas.json` → `production.env.API_BASE_URL`:
```json
"env": {
  "API_BASE_URL": "https://api.sahayak.health"
}
```

## Common Issues

### Build Fails
```bash
# Clear cache and rebuild
eas build --platform android --profile preview --clear-cache
```

### Metro Bundler Issues
```bash
npx expo start --clear
```

### Native Module Not Found
```bash
# Prebuild native code (creates android/ folder)
npx expo prebuild --platform android
```

## App Store Submission

1. Build production AAB:
   ```bash
   eas build --platform android --profile production
   ```

2. Submit to Play Store:
   ```bash
   eas submit --platform android
   ```

3. Configure `eas.json` submit profile with Play Store credentials.

## Version Management

Update in `app.json` before each release:
```json
{
  "expo": {
    "version": "1.0.1",
    "android": {
      "versionCode": 2
    }
  }
}
```

- `version`: User-visible version string
- `versionCode`: Must increment for each Play Store upload
