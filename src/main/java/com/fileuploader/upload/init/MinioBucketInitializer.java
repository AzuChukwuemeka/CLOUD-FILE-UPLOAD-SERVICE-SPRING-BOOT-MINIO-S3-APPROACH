package com.fileuploader.upload.init;

import com.fileuploader.upload.config.MinioProperties;
import com.fileuploader.upload.services.storage.BlobStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Ensures the configured MinIO bucket exists on startup.
 * Disabled under the "test" profile so unit/integration tests don't need a running MinIO instance.
 */
@Component
@Profile("!test")
@EnableConfigurationProperties(MinioProperties.class)
public class MinioBucketInitializer {

    private static final Logger log = LoggerFactory.getLogger(MinioBucketInitializer.class);

    private final BlobStore blobStore;
    private final MinioProperties minioProperties;

    public MinioBucketInitializer(BlobStore blobStore, MinioProperties minioProperties) {
        this.blobStore = blobStore;
        this.minioProperties = minioProperties;
    }

    @PostConstruct
    public void ensureBucketExists() {
        try {
            String bucketName = minioProperties.getBucketName();
            if (blobStore.bucketExists(bucketName)) {
                return;
            }
            log.info("Bucket '{}' does not exist yet, creating it", bucketName);
            blobStore.createBucket(bucketName);
        } catch (Exception e) {
            log.warn("Could not reach MinIO on startup ({}). File upload/download will fail until it is reachable.", e.getMessage());
        }
    }
}
