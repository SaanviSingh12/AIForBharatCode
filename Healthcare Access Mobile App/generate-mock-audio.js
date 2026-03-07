#!/usr/bin/env node

/**
 * Generate Mock Audio Base64 for Testing
 * 
 * This script helps you convert an MP3 file to base64 for testing the audio player.
 * 
 * Usage:
 *   node generate-mock-audio.js <path-to-mp3-file>
 * 
 * Example:
 *   node generate-mock-audio.js ./sample-response.mp3
 */

const fs = require('fs');
const path = require('path');

// Get the MP3 file path from command line arguments
const args = process.argv.slice(2);

if (args.length === 0) {
    console.log('❌ Error: Please provide an MP3 file path\n');
    console.log('Usage: node generate-mock-audio.js <path-to-mp3-file>\n');
    console.log('Example: node generate-mock-audio.js ./sample-audio.mp3\n');
    console.log('💡 Tip: You can generate audio at https://ttsmp3.com/');
    process.exit(1);
}

const mp3FilePath = args[0];

// Check if file exists
if (!fs.existsSync(mp3FilePath)) {
    console.log(`❌ Error: File not found: ${mp3FilePath}`);
    process.exit(1);
}

// Check if it's an MP3 file
const ext = path.extname(mp3FilePath).toLowerCase();
if (ext !== '.mp3') {
    console.log(`⚠️  Warning: File extension is ${ext}, expected .mp3`);
    console.log('Continuing anyway...\n');
}

try {
    // Read the MP3 file
    console.log(`📖 Reading file: ${mp3FilePath}`);
    const audioBuffer = fs.readFileSync(mp3FilePath);

    // Convert to base64
    console.log('🔄 Converting to base64...');
    const base64Audio = audioBuffer.toString('base64');

    // Get file size info
    const fileSizeKB = (audioBuffer.length / 1024).toFixed(2);
    const base64SizeKB = (base64Audio.length / 1024).toFixed(2);

    console.log(`✅ Conversion successful!`);
    console.log(`📊 Original file size: ${fileSizeKB} KB`);
    console.log(`📊 Base64 string size: ${base64SizeKB} KB\n`);

    // Save to file
    const outputFile = './mock-audio-base64.txt';
    fs.writeFileSync(outputFile, base64Audio);
    console.log(`💾 Base64 string saved to: ${outputFile}\n`);

    // Show preview
    const preview = base64Audio.substring(0, 100) + '...';
    console.log('📋 Preview (first 100 chars):');
    console.log(preview);
    console.log('\n📝 Next steps:');
    console.log('1. Copy the base64 string from mock-audio-base64.txt');
    console.log('2. Replace the mockAudioBase64 value in SymptomAnalysis.tsx (around line 98)');
    console.log('3. Test the audio player in your app!\n');

} catch (error) {
    console.log(`❌ Error: ${error.message}`);
    process.exit(1);
}
