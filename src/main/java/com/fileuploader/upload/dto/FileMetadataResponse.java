package com.fileuploader.upload.dto;

import java.util.UUID;

public record FileMetadataResponse(
        UUID id,
        String fileName,
        String contentType,
        long fileSize,
        String uploadedBy
) {
}
