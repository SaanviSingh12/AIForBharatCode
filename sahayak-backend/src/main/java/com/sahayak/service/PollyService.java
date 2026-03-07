package com.sahayak.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.model.*;

import java.util.Base64;

@Slf4j
@Service
public class PollyService {

    private final PollyClient pollyClient;

    public PollyService(PollyClient pollyClient) {
        this.pollyClient = pollyClient;
    }

    /**
     * Synthesizes text to speech and returns a Base64 encoded MP3.
     * Kajal (Neural) supports hi-IN natively and en-IN.
     * For other Indian languages, Bedrock generates the text in the correct script;
     * Polly uses hi-IN for Devanagari-script languages and en-IN for others.
     * @param text     Text to speak
     * @param language Language code: "hi-IN" | "te-IN" | "ta-IN" | "kn-IN" | etc.
     * @return Base64 encoded MP3 string, or null if TTS fails (graceful degradation)
     */
    public String synthesize(String text, String language) {
        try {
            // Trim text to avoid exceeding Polly's 3000 char limit
            if (text.length() > 2800) {
                text = text.substring(0, 2800) + "...";
            }

            VoiceId voiceId = resolveVoiceId(language);
            String langCode = resolveLanguageCode(language);

            SynthesizeSpeechRequest request = SynthesizeSpeechRequest.builder()
                    .text(text)
                    .voiceId(voiceId)
                    .outputFormat(OutputFormat.MP3)
                    .engine(Engine.NEURAL)
                    .languageCode(LanguageCode.fromValue(langCode))
                    .build();

            // AWS SDK v2: synthesizeSpeech returns ResponseInputStream<SynthesizeSpeechResponse>
            ResponseInputStream<SynthesizeSpeechResponse> audioStream = pollyClient.synthesizeSpeech(request);
            byte[] audioBytes = audioStream.readAllBytes();
            log.info("Polly synthesized {} bytes of audio", audioBytes.length);
            return Base64.getEncoder().encodeToString(audioBytes);

        } catch (Exception e) {
            log.warn("Polly TTS failed, returning null (text-only fallback): {}", e.getMessage());
            return null;
        }
    }

    private VoiceId resolveVoiceId(String language) {
        return switch (language) {
            case "hi-IN" -> VoiceId.KAJAL;    // Hindi neural voice (female, Indian accent)
            case "ta-IN" -> VoiceId.KAJAL;    // Use Kajal as fallback for Tamil
            case "bn-IN" -> VoiceId.KAJAL;
            default      -> VoiceId.KAJAL;    // Kajal supports multiple Indian languages
        };
    }

    /**
     * Resolves the Polly language code. Kajal supports hi-IN and en-IN natively.
     * For all non-English Indian languages, the audio text is already translated
     * to Hindi by Bedrock (via responseForAudio), so we always use hi-IN.
     */
    private String resolveLanguageCode(String language) {
        return "en-IN".equals(language) ? "en-IN" : "hi-IN";
    }
}
