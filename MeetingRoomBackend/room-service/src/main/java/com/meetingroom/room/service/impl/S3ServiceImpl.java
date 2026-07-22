package com.meetingroom.room.service.impl;

import com.meetingroom.room.exception.BusinessException;
import com.meetingroom.room.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    @Value("${aws.s3.access-key}")
    private String accessKey;

    @Override
    public String uploadFile(MultipartFile file) {
        log.info("Uploading multipart file '{}' to S3 bucket: {}", file.getOriginalFilename(), bucketName);

        boolean customCredentialsSet = accessKey != null && !accessKey.trim().isEmpty();
        boolean standardEnvCredentialsSet = System.getenv("AWS_ACCESS_KEY_ID") != null || System.getProperty("aws.accessKeyId") != null;

        if (!customCredentialsSet && !standardEnvCredentialsSet) {
            throw new BusinessException("AWS S3 credentials are not configured. Please supply AWS_ACCESS_KEY and AWS_SECRET_KEY as custom properties, or AWS_ACCESS_KEY_ID and AWS_SECRET_ACCESS_KEY environment variables.");
        }

        if (file.isEmpty()) {
            throw new BusinessException("Cannot upload an empty file");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        // Generate unique key for S3 object
        String key = "rooms/" + UUID.randomUUID().toString() + fileExtension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // Generate public URL
            String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
            log.info("File uploaded successfully. Public S3 URL: {}", s3Url);
            return s3Url;

        } catch (IOException e) {
            log.error("Failed to read file input stream", e);
            throw new BusinessException("Failed to upload room image: " + e.getMessage());
        } catch (Exception e) {
            log.error("AWS S3 client execution failed", e);
            throw new BusinessException("AWS S3 upload error: " + e.getMessage());
        }
    }
}
