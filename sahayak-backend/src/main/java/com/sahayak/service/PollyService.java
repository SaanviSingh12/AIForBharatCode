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
     * @param text     Text to speak
     * @param language Language code: "hi-IN" | "ta-IN" | "en-IN"
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

    private String resolveLanguageCode(String language) {
        return switch (language) {
            case "hi-IN" -> "hi-IN";
            case "ta-IN" -> "en-IN";   // Polly Tamil uses en-IN with Kajal
            default      -> "en-IN";
        };
    }
}
