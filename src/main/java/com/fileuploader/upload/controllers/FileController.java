package com.fileuploader.upload.controllers;

import com.fileuploader.upload.dto.FileMetadataResponse;
import com.fileuploader.upload.services.FileService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/files")
@Tag(name = "Files", description = "Upload, list, download, and delete files. Every endpoint here requires HTTP Basic authentication, and users can only see and manage their own files.")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    @PostMapping(consumes = "multipart/form-data")
    @Operation(summary = "Upload a file", description = "Uploads a file as multipart/form-data and stores it in the configured object storage bucket. The file is associated with the authenticated user.")
    public ResponseEntity<FileMetadataResponse> uploadFile(@RequestParam("file") MultipartFile file, Authentication authentication) throws IOException {
        FileMetadataResponse response = fileService.uploadFile(
                file.getInputStream(),
                file.getOriginalFilename(),
                file.getContentType(),
                file.getSize(),
                authentication.getName()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List my files", description = "Returns metadata for every file uploaded by the authenticated user.")
    public List<FileMetadataResponse> getMyFiles(Authentication authentication) {
        return fileService.getFilesForUser(authentication.getName());
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get file metadata", description = "Returns metadata for a single file, if it belongs to the authenticated user.")
    public FileMetadataResponse getFileMetadata(@PathVariable UUID id, Authentication authentication) {
        return fileService.getFileMetadata(id, authentication.getName());
    }

    @GetMapping("/{id}/download")
    @Operation(summary = "Download a file", description = "Streams the raw file content back to the client, if it belongs to the authenticated user.")
    public void downloadFile(@PathVariable UUID id, Authentication authentication, HttpServletResponse response) {
        fileService.downloadFile(id, authentication.getName(), response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a file", description = "Deletes a file's content from object storage and removes its metadata, if it belongs to the authenticated user.")
    public ResponseEntity<Void> deleteFile(@PathVariable UUID id, Authentication authentication) {
        fileService.deleteFile(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
