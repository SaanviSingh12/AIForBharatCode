package com.sahayak.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.transcribe.TranscribeClient;
import software.amazon.awssdk.services.transcribe.model.*;

import java.util.UUID;

@Slf4j
@Service
public class TranscribeService {

    private final TranscribeClient transcribeClient;
    private final S3Service s3Service;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public TranscribeService(TranscribeClient transcribeClient, S3Service s3Service) {
        this.transcribeClient = transcribeClient;
        this.s3Service = s3Service;
    }

    /**
     * Transcribes audio from an S3 key. Returns the transcribed text.
     * Polls for up to 60 seconds before giving up.
     */
    public String transcribeAudio(String s3Key, String language) {
        String jobName = "sahayak-" + UUID.randomUUID();
        String s3Uri = "s3://" + bucketName + "/" + s3Key;
        String languageCode = mapLanguageCode(language);

        log.info("Starting Transcribe job {} for language {}", jobName, languageCode);

        StartTranscriptionJobRequest startRequest = StartTranscriptionJobRequest.builder()
                .transcriptionJobName(jobName)
                .media(Media.builder().mediaFileUri(s3Uri).build())
                .languageCode(languageCode)
                .outputBucketName(bucketName)
                .outputKey("transcripts/" + jobName + ".json")
                .build();

        transcribeClient.startTranscriptionJob(startRequest);

        // Poll until done (max 60 seconds)
        for (int i = 0; i < 12; i++) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }

            GetTranscriptionJobResponse jobResponse = transcribeClient.getTranscriptionJob(
                    GetTranscriptionJobRequest.builder().transcriptionJobName(jobName).build()
            );

            TranscriptionJobStatus status = jobResponse.transcriptionJob().transcriptionJobStatus();
            log.info("Transcribe job {} status: {}", jobName, status);

            if (status == TranscriptionJobStatus.COMPLETED) {
                // Download the transcript JSON from S3 (output was saved to S3)
                try {
                    String transcriptKey = "transcripts/" + jobName + ".json";
                    software.amazon.awssdk.services.s3.model.GetObjectRequest getReq =
                            software.amazon.awssdk.services.s3.model.GetObjectRequest.builder()
                                    .bucket(bucketName)
                                    .key(transcriptKey)
                                    .build();
                    // We'll parse the JSON using Jackson via ObjectMapper
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    byte[] bytes = null;
                    try (var s3Client = software.amazon.awssdk.services.s3.S3Client.builder().build()) {
                        bytes = s3Client.getObject(getReq).readAllBytes();
                    }
                    com.fasterxml.jackson.databind.JsonNode root = mapper.readTree(bytes);
                    String transcript = root.path("results").path("transcripts").get(0).path("transcript").asText();
                    log.info("Transcription complete: {}", transcript);
                    return transcript;
                } catch (Exception e) {
                    log.error("Failed to download transcript: {}", e.getMessage());
                    return "";
                }
            } else if (status == TranscriptionJobStatus.FAILED) {
                log.error("Transcribe job failed: {}", jobResponse.transcriptionJob().failureReason());
                return "";
            }
        }

        log.warn("Transcribe job {} timed out", jobName);
        return "";
    }

    private String mapLanguageCode(String language) {
        if (language == null) return "hi-IN";
        return switch (language.toLowerCase()) {
            case "hi", "hi-in" -> "hi-IN";
            case "ta", "ta-in" -> "ta-IN";
            case "te", "te-in" -> "te-IN";
            case "kn", "kn-in" -> "kn-IN";
            case "mr", "mr-in" -> "mr-IN";
            default -> "en-IN";
        };
    }
}
