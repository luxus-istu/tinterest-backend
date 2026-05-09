package com.luxus.tinterest.service;

import com.luxus.tinterest.configuration.MinioProperties;
import com.luxus.tinterest.exception.profile.InvalidAvatarFileException;
import com.luxus.tinterest.exception.profile.StorageOperationException;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.SetBucketPolicyArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("MinioStorageService Unit Tests")
class MinioStorageServiceTests {

    @Mock
    private MinioClient minioClient;

    @Mock
    private MinioProperties minioProperties;

    @InjectMocks
    private MinioStorageService minioStorageService;

    @BeforeEach
    void setUp() throws Exception {
        Field bucketReadyField = MinioStorageService.class.getDeclaredField("bucketReady");
        bucketReadyField.setAccessible(true);
        bucketReadyField.set(minioStorageService, new java.util.concurrent.atomic.AtomicBoolean(true));
    }

    @Test
    @DisplayName("Should upload avatar and return public url")
    void testUploadAvatarReturnsUrl() throws Exception {
        when(minioProperties.getBucket()).thenReturn("avatars");
        when(minioProperties.getPublicEndpoint()).thenReturn("https://public.example.com");


        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "pngdata".getBytes()
        );

        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(null);

        String url = minioStorageService.uploadAvatar(file, 10L, null);

        assertTrue(url.startsWith("https://public.example.com/avatars/avatars/"));
        verify(minioClient).putObject(any(PutObjectArgs.class));
    }

    @Test
    @DisplayName("Should throw for unsupported avatar content type")
    void testUploadAvatarThrowsInvalidContentType() {
        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.txt",
                "text/plain",
                "data".getBytes()
        );

        assertThrows(InvalidAvatarFileException.class,
                () -> minioStorageService.uploadAvatar(file, 10L, null));
    }

    @Test
    @DisplayName("Should wrap Minio exceptions into StorageOperationException")
    void testUploadAvatarThrowsStorageOperationException() throws Exception {
        when(minioProperties.getBucket()).thenReturn("avatars");

        MultipartFile file = new MockMultipartFile(
                "avatar",
                "avatar.png",
                "image/png",
                "pngdata".getBytes()
        );

        doThrow(new RuntimeException("fail"))
                .when(minioClient).putObject(any(PutObjectArgs.class));

        assertThrows(StorageOperationException.class,
                () -> minioStorageService.uploadAvatar(file, 10L, null));
    }
}
