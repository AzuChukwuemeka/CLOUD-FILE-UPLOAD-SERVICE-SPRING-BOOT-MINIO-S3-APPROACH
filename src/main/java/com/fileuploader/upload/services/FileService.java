package com.fileuploader.upload.services;

import com.fileuploader.upload.config.MinioProperties;
import com.fileuploader.upload.dto.FileMetadataResponse;
import com.fileuploader.upload.entities.FileMetadata;
import com.fileuploader.upload.exceptions.ResourceNotFoundException;
import com.fileuploader.upload.exceptions.StorageException;
import com.fileuploader.upload.repositories.FileMetadataRepository;
import com.fileuploader.upload.services.storage.BlobStore;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    private final FileMetadataRepository fileMetadataRepository;
    private final BlobStore blobStore;
    private final MinioProperties minioProperties;

    public FileService(FileMetadataRepository fileMetadataRepository, BlobStore blobStore, MinioProperties minioProperties) {
        this.fileMetadataRepository = fileMetadataRepository;
        this.blobStore = blobStore;
        this.minioProperties = minioProperties;
    }

    public FileMetadataResponse uploadFile(InputStream inputStream, String originalFilename, String contentType, long fileSize, String uploadedBy) {
        UUID fileId = UUID.randomUUID();
        String storedName = fileId + "-" + originalFilename;
        try {
            blobStore.uploadFile(minioProperties.getBucketName(), storedName, inputStream);
        } catch (Exception e) {
            throw new StorageException("Failed to upload file to object storage", e);
        }

        FileMetadata metadata = new FileMetadata(fileId, originalFilename, storedName, contentType, fileSize, uploadedBy);
        FileMetadata saved = fileMetadataRepository.save(metadata);
        return toResponse(saved);
    }

    public List<FileMetadataResponse> getFilesForUser(String uploadedBy) {
        return fileMetadataRepository.findByUploadedBy(uploadedBy).stream()
                .map(this::toResponse)
                .toList();
    }

    public FileMetadataResponse getFileMetadata(UUID id, String requestingUser) {
        return toResponse(getOwnedFileOrThrow(id, requestingUser));
    }

    public void downloadFile(UUID id, String requestingUser, HttpServletResponse response) {
        FileMetadata metadata = getOwnedFileOrThrow(id, requestingUser);
        try {
            response.setContentType(metadata.getContentType());
            response.setHeader("Content-Disposition", "attachment; filename=\"" + metadata.getFileName() + "\"");
            blobStore.downloadFile(minioProperties.getBucketName(), metadata.getStoredName(), response.getOutputStream());
        } catch (IOException e) {
            throw new StorageException("Failed to stream file to the client", e);
        } catch (Exception e) {
            throw new StorageException("Failed to download file from object storage", e);
        }
    }

    public void deleteFile(UUID id, String requestingUser) {
        FileMetadata metadata = getOwnedFileOrThrow(id, requestingUser);
        try {
            blobStore.deleteFile(minioProperties.getBucketName(), metadata.getStoredName());
        } catch (Exception e) {
            throw new StorageException("Failed to delete file from object storage", e);
        }
        fileMetadataRepository.deleteById(id);
    }

    private FileMetadata getOwnedFileOrThrow(UUID id, String requestingUser) {
        FileMetadata metadata = fileMetadataRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("No file found with id " + id));
        if (!metadata.getUploadedBy().equals(requestingUser)) {
            throw new ResourceNotFoundException("No file found with id " + id);
        }
        return metadata;
    }

    private FileMetadataResponse toResponse(FileMetadata metadata) {
        return new FileMetadataResponse(
                metadata.getId(),
                metadata.getFileName(),
                metadata.getContentType(),
                metadata.getFileSize(),
                metadata.getUploadedBy()
        );
    }
}
