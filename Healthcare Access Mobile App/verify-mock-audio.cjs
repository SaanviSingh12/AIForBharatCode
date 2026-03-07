#!/usr/bin/env node

/**
 * Verify Mock Audio Base64 is Valid MP3
 */

const fs = require('fs');

// The mock audio base64 from SymptomAnalysis.tsx
const mockAudioBase64 = 'SUQzBAAAAAAAI1RTU0UAAAAPAAADTGF2ZjU4Ljc2LjEwMAAAAAAAAAAAAAAA//tQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAWGluZwAAAA8AAAACAAADhAC7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7u7v////////////////////////////////////////////////////////////AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA//sQZAAP8AAAaQAAAAgAAA0gAAABAAABpAAAACAAADSAAAAETEFN//sQZDIP8AAAaQAAAAgAAA0gAAABAAABpAAAACAAADSAAAAEUzLjEwMFVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVVQ==';

console.log('🔍 Verifying Mock Audio Base64...\n');

try {
    // Decode base64 to buffer
    const audioBuffer = Buffer.from(mockAudioBase64, 'base64');
    
    console.log(`📊 File size: ${audioBuffer.length} bytes (${(audioBuffer.length / 1024).toFixed(2)} KB)\n`);
    
    // Check for ID3 tag (MP3 metadata header)
    const id3Header = audioBuffer.slice(0, 3).toString();
    console.log(`🏷️  ID3 Header: "${id3Header}"`);
    
    if (id3Header === 'ID3') {
        console.log('   ✅ Valid ID3 tag found - This is an MP3 file!\n');
        
        // Get ID3 version
        const id3Version = audioBuffer[3];
        const id3Revision = audioBuffer[4];
        console.log(`   ID3 Version: 2.${id3Version}.${id3Revision}`);
        
    } else {
        console.log('   ⚠️  No ID3 tag found\n');
    }
    
    // Look for MP3 frame sync markers (0xFF 0xFB or 0xFF 0xFA or similar)
    let frameFound = false;
    for (let i = 0; i < audioBuffer.length - 1; i++) {
        if (audioBuffer[i] === 0xFF && (audioBuffer[i + 1] & 0xE0) === 0xE0) {
            console.log(`🎵 MP3 Frame sync marker found at byte ${i}`);
            console.log('   ✅ Valid MP3 audio frame detected!\n');
            frameFound = true;
            break;
        }
    }
    
    if (!frameFound) {
        console.log('⚠️  No MP3 frame sync markers found\n');
    }
    
    // Check for "LAME" encoder tag (common in MP3s)
    const lameIndex = audioBuffer.indexOf('LAME');
    if (lameIndex > -1) {
        const lameVersion = audioBuffer.slice(lameIndex, lameIndex + 20).toString('ascii').replace(/\0/g, '');
        console.log(`🎼 LAME Encoder: ${lameVersion}`);
        console.log('   ✅ Encoded with LAME MP3 encoder\n');
    }
    
    // Check for "Xing" header (VBR MP3)
    const xingIndex = audioBuffer.indexOf('Xing');
    if (xingIndex > -1) {
        console.log(`📦 Xing VBR Header found at byte ${xingIndex}`);
        console.log('   ✅ Variable Bit Rate MP3\n');
    }
    
    // Save to file for testing
    fs.writeFileSync('./test-mock-audio.mp3', audioBuffer);
    console.log('💾 Audio saved to: test-mock-audio.mp3');
    console.log('   You can test playback with: ffplay test-mock-audio.mp3\n');
    
    // Final verdict
    console.log('━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n');
    if (id3Header === 'ID3' && frameFound) {
        console.log('✅ VERDICT: This IS a valid MP3 file!');
        console.log('   Format: MP3 with ID3 metadata');
        console.log('   Status: Ready for use\n');
    } else {
        console.log('⚠️  VERDICT: MP3 structure detected but may be minimal');
        console.log('   This is likely a stub/silent MP3 for demo purposes\n');
    }
    
} catch (error) {
    console.log(`❌ Error: ${error.message}`);
    process.exit(1);
}
