# Mock Audio File Guide

## Current Mock Audio

The application currently uses a minimal MP3 base64 string in `SymptomAnalysis.tsx` for demo purposes. This is a very short silent audio file.

## How to Test with Real Audio

### Option 1: Use AWS Polly (Production)
When the backend is properly configured with AWS credentials, AWS Polly will automatically generate audio responses.

### Option 2: Generate Your Own Mock Audio

#### Using Online Tools:
1. Go to https://ttsmp3.com/
2. Enter text: "आपके लक्षणों के आधार पर, मैं एक सामान्य चिकित्सक से परामर्श करने की सलाह देता हूं"
3. Select language: Hindi
4. Download the MP3 file

#### Convert to Base64:
```bash
# On macOS/Linux:
base64 -i yourfile.mp3 -o audio.txt

# On Windows (PowerShell):
[Convert]::ToBase64String([IO.File]::ReadAllBytes("yourfile.mp3")) > audio.txt
```

#### Update Mock Audio:
Replace the `mockAudioBase64` value in `SymptomAnalysis.tsx` (around line 98) with your base64 string.

### Option 3: Using Node.js Script

Create a file `generate-mock-audio.js`:

```javascript
const fs = require('fs');

// Read your MP3 file
const audioBuffer = fs.readFileSync('./sample-audio.mp3');
const base64Audio = audioBuffer.toString('base64');

console.log('Base64 Audio String (copy this to SymptomAnalysis.tsx):');
console.log(base64Audio);

// Optionally save to file
fs.writeFileSync('./audio-base64.txt', base64Audio);
```

Run with: `node generate-mock-audio.js`

## Testing the Audio Player

Once you have a valid audio base64 string:

1. Navigate through symptom entry (text or voice)
2. The mock audio will be set along with the mock response text
3. On the Doctor Search or Emergency page, a floating audio player will appear at the bottom
4. Click "Play Audio Response" to hear the audio

## API Integration

In production, the backend returns this structure:

```json
{
  "success": true,
  "responseText": "Patient-friendly response in user's language",
  "audioBase64": "base64-encoded-mp3-from-aws-polly",
  ...
}
```

The frontend automatically uses this audio if available, falling back to the mock if needed.
