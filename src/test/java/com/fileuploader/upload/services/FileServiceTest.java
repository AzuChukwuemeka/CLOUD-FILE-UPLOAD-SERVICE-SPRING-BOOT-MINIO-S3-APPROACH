package com.fileuploader.upload.services;

import com.fileuploader.upload.config.MinioProperties;
import com.fileuploader.upload.dto.FileMetadataResponse;
import com.fileuploader.upload.entities.FileMetadata;
import com.fileuploader.upload.exceptions.ResourceNotFoundException;
import com.fileuploader.upload.exceptions.StorageException;
import com.fileuploader.upload.repositories.FileMetadataRepository;
import com.fileuploader.upload.services.storage.BlobStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private FileMetadataRepository fileMetadataRepository;

    @Mock
    private BlobStore blobStore;

    private FileService fileService;

    private MinioProperties minioProperties;

    @BeforeEach
    void setUp() {
        minioProperties = new MinioProperties();
        minioProperties.setBucketName("test-bucket");
        fileService = new FileService(fileMetadataRepository, blobStore, minioProperties);
    }

    @Test
    void uploadFile_storesBlobAndMetadata() throws Exception {
        when(fileMetadataRepository.save(any(FileMetadata.class))).thenAnswer(inv -> inv.getArgument(0));

        FileMetadataResponse response = fileService.uploadFile(
                new ByteArrayInputStream("hello".getBytes()), "notes.txt", "text/plain", 5L, "user@example.com");

        assertThat(response.fileName()).isEqualTo("notes.txt");
        assertThat(response.uploadedBy()).isEqualTo("user@example.com");
        verify(blobStore).uploadFile(eq("test-bucket"), anyString(), any());
    }

    @Test
    void uploadFile_wrapsStorageFailures() throws Exception {
        doThrow(new RuntimeException("network down")).when(blobStore).uploadFile(anyString(), anyString(), any());

        assertThatThrownBy(() -> fileService.uploadFile(
                new ByteArrayInputStream("hello".getBytes()), "notes.txt", "text/plain", 5L, "user@example.com"))
                .isInstanceOf(StorageException.class);

        verify(fileMetadataRepository, never()).save(any());
    }

    @Test
    void getFileMetadata_throwsWhenOwnedByAnotherUser() {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = new FileMetadata(id, "notes.txt", "stored-notes.txt", "text/plain", 5L, "owner@example.com");
        when(fileMetadataRepository.findById(id)).thenReturn(Optional.of(metadata));

        assertThatThrownBy(() -> fileService.getFileMetadata(id, "someone-else@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getFileMetadata_throwsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(fileMetadataRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileService.getFileMetadata(id, "user@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getFilesForUser_returnsMappedResponses() {
        FileMetadata metadata = new FileMetadata(UUID.randomUUID(), "a.txt", "stored-a.txt", "text/plain", 3L, "user@example.com");
        when(fileMetadataRepository.findByUploadedBy("user@example.com")).thenReturn(List.of(metadata));

        List<FileMetadataResponse> result = fileService.getFilesForUser("user@example.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).fileName()).isEqualTo("a.txt");
    }

    @Test
    void deleteFile_removesBlobAndMetadataWhenOwned() throws Exception {
        UUID id = UUID.randomUUID();
        FileMetadata metadata = new FileMetadata(id, "a.txt", "stored-a.txt", "text/plain", 3L, "user@example.com");
        when(fileMetadataRepository.findById(id)).thenReturn(Optional.of(metadata));

        fileService.deleteFile(id, "user@example.com");

        verify(blobStore).deleteFile("test-bucket", "stored-a.txt");
        verify(fileMetadataRepository).deleteById(id);
    }
}
