package com.fileuploader.upload.services.storage;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Abstraction over an S3-compatible object store so the rest of the
 * application never has to depend directly on the MinIO SDK.
 */
public interface BlobStore {

    boolean bucketExists(String bucketName) throws Exception;

    void createBucket(String bucketName) throws Exception;

    void uploadFile(String bucketName, String objectName, InputStream inputStream) throws Exception;

    void downloadFile(String bucketName, String objectName, OutputStream outputStream) throws Exception;

    void deleteFile(String bucketName, String objectName) throws Exception;
}
