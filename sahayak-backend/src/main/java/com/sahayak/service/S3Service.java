package com.sahayak.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    public S3Service(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    /**
     * Uploads an audio file to S3 and returns its S3 key.
     */
    public String uploadAudio(MultipartFile file) throws IOException {
        String key = "audio/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        upload(key, file.getBytes(), file.getContentType());
        log.info("Uploaded audio to S3: {}", key);
        return key;
    }

    /**
     * Uploads an image file to S3 and returns its S3 key.
     */
    public String uploadImage(MultipartFile file) throws IOException {
        String key = "images/" + UUID.randomUUID() + "-" + file.getOriginalFilename();
        upload(key, file.getBytes(), file.getContentType());
        log.info("Uploaded image to S3: {}", key);
        return key;
    }

    private void upload(String key, byte[] bytes, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType != null ? contentType : "application/octet-stream")
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(bytes));
    }

    /**
     * Deletes a file from S3. Always call this after processing to keep bucket clean.
     */
    public void deleteFile(String key) {
        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            log.info("Deleted S3 object: {}", key);
        } catch (Exception e) {
            log.warn("Failed to delete S3 object {}: {}", key, e.getMessage());
        }
    }
}
