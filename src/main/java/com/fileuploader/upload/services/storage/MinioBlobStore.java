package com.fileuploader.upload.services.storage;

import io.minio.*;
import org.springframework.stereotype.Service;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.OutputStream;

@Service
public class MinioBlobStore implements BlobStore {

    private final MinioClient minioClient;

    public MinioBlobStore(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public boolean bucketExists(String bucketName) throws Exception {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    @Override
    public void createBucket(String bucketName) throws Exception {
        minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    }

    @Override
    public void uploadFile(String bucketName, String objectName, InputStream inputStream) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(objectName)
                        .stream(new BufferedInputStream(inputStream), -1, 10_485_760)
                        .build()
        );
    }

    @Override
    public void downloadFile(String bucketName, String objectName, OutputStream outputStream) throws Exception {
        try (GetObjectResponse object = minioClient.getObject(
                GetObjectArgs.builder().bucket(bucketName).object(objectName).build())) {
            object.transferTo(outputStream);
        }
    }

    @Override
    public void deleteFile(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }
}
