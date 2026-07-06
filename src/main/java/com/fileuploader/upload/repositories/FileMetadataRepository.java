package com.fileuploader.upload.repositories;

import com.fileuploader.upload.entities.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, UUID> {
    List<FileMetadata> findByUploadedBy(String uploadedBy);
}
