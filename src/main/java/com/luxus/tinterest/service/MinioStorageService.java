package com.luxus.tinterest.service;

import com.luxus.tinterest.configuration.MinioProperties;
import com.luxus.tinterest.exception.profile.InvalidAvatarFileException;
import com.luxus.tinterest.exception.profile.StorageOperationException;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
@RequiredArgsConstructor
@Slf4j
public class MinioStorageService {

    private static final Map<String, String> CONTENT_TYPE_TO_EXTENSION = Map.of(
            "image/jpeg", ".jpg",
            "image/jpg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );

    private final MinioClient minioClient;
    private final MinioProperties minioProperties;
    private final AtomicBoolean bucketReady = new AtomicBoolean(false);

    public String uploadAvatar(MultipartFile file, Long userId, String currentAvatarUrl) {
        log.info("Uploading avatar for user ID: {}", userId);
        validateAvatar(file);
        ensureBucketIsReady();

        if (currentAvatarUrl != null && !currentAvatarUrl.isBlank()) {
            log.info("Deleting old avatar: {}", currentAvatarUrl);
            deleteAvatar(currentAvatarUrl);
        }

        String contentType = file.getContentType().toLowerCase(Locale.ROOT);
        String objectName = "avatars/%d/%s%s".formatted(userId, UUID.randomUUID(), CONTENT_TYPE_TO_EXTENSION.get(contentType));

        try (InputStream inputStream = file.getInputStream()) {
            log.info("Saving object {} to bucket {}", objectName, minioProperties.getBucket());
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(contentType)
                            .build()
            );
            String url = buildPublicUrl(objectName);
            log.info("Avatar uploaded successfully. Public URL: {}", url);
            return url;
        } catch (Exception ex) {
            log.error("Failed to upload avatar to MinIO for user {}", userId, ex);
            throw new StorageOperationException("Failed to upload avatar to object storage", ex);
        }
    }

    private void validateAvatar(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            log.warn("Avatar upload failed: file is empty");
            throw new InvalidAvatarFileException("Avatar file must not be empty");
        }

        String contentType = file.getContentType();
        if (contentType == null || !CONTENT_TYPE_TO_EXTENSION.containsKey(contentType.toLowerCase(Locale.ROOT))) {
            log.warn("Avatar upload failed: unsupported content type {}", contentType);
            throw new InvalidAvatarFileException("Only JPG, PNG and WEBP images are supported");
        }
    }

    private void ensureBucketIsReady() {
        if (bucketReady.get()) {
            return;
        }

        try {
            log.info("Checking if bucket {} exists", minioProperties.getBucket());
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(minioProperties.getBucket()).build()
            );

            if (!exists) {
                log.info("Creating bucket {}", minioProperties.getBucket());
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(minioProperties.getBucket()).build());
            }

            log.info("Setting public read policy for bucket {}", minioProperties.getBucket());
            minioClient.setBucketPolicy(
                    SetBucketPolicyArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .config("""
                                    {
                                      "Version": "2012-10-17",
                                      "Statement": [
                                        {
                                          "Effect": "Allow",
                                          "Principal": {
                                            "AWS": ["*"]
                                          },
                                          "Action": ["s3:GetObject"],
                                          "Resource": ["arn:aws:s3:::%s/*"]
                                        }
                                      ]
                                    }
                                    """.formatted(minioProperties.getBucket()))
                            .build()
            );

            bucketReady.set(true);
        } catch (Exception ex) {
            log.error("Failed to initialize MinIO bucket {}", minioProperties.getBucket(), ex);
            throw new StorageOperationException("Failed to initialize MinIO bucket", ex);
        }
    }

    private void deleteAvatar(String avatarUrl) {
        String bucketPrefix = buildPublicUrl("");
        if (!avatarUrl.startsWith(bucketPrefix)) {
            log.warn("Avatar URL {} does not match bucket prefix {}. Skipping deletion.", avatarUrl, bucketPrefix);
            return;
        }

        String objectName = avatarUrl.substring(bucketPrefix.length());
        try {
            log.info("Removing object {} from bucket {}", objectName, minioProperties.getBucket());
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(minioProperties.getBucket())
                            .object(objectName)
                            .build()
            );
        } catch (Exception ex) {
            log.error("Failed to remove object {} from MinIO", objectName, ex);
            throw new StorageOperationException("Failed to replace the existing avatar in object storage", ex);
        }
    }

    private String buildPublicUrl(String objectName) {
        String endpoint = minioProperties.getPublicEndpoint();
        if (endpoint.endsWith("/")) {
            endpoint = endpoint.substring(0, endpoint.length() - 1);
        }
        return "%s/%s/%s".formatted(endpoint, minioProperties.getBucket(), objectName);
    }
}
