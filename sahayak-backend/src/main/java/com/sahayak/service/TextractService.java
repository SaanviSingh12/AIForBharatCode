package com.sahayak.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.textract.TextractClient;
import software.amazon.awssdk.services.textract.model.*;

import java.util.stream.Collectors;

@Slf4j
@Service
public class TextractService {

    private final TextractClient textractClient;

    @Value("${aws.s3.bucket}")
    private String s3Bucket;

    public TextractService(TextractClient textractClient) {
        this.textractClient = textractClient;
    }

    /**
     * Extracts text from raw image bytes (e.g., uploaded via multipart form)
     */
    public String extractTextFromBytes(byte[] imageBytes) {
        log.info("Running Textract OCR on {} bytes", imageBytes.length);

        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                        .bytes(SdkBytes.fromByteArray(imageBytes))
                        .build())
                .build();

        DetectDocumentTextResponse response = textractClient.detectDocumentText(request);

        String extracted = response.blocks().stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .map(Block::text)
                .collect(Collectors.joining("\n"));

        log.info("Textract extracted {} characters", extracted.length());
        return extracted;
    }

    /**
     * Extracts text from image already stored in S3
     */
    public String extractTextFromS3(String s3Key) {
        log.info("Running Textract OCR on S3 key: {}", s3Key);

        DetectDocumentTextRequest request = DetectDocumentTextRequest.builder()
                .document(Document.builder()
                        .s3Object(S3Object.builder()
                                .bucket(s3Bucket)
                                .name(s3Key)
                                .build())
                        .build())
                .build();

        DetectDocumentTextResponse response = textractClient.detectDocumentText(request);

        String extracted = response.blocks().stream()
                .filter(block -> block.blockType() == BlockType.LINE)
                .map(Block::text)
                .collect(Collectors.joining("\n"));

        log.info("Textract (S3) extracted {} characters", extracted.length());
        return extracted;
    }
}
