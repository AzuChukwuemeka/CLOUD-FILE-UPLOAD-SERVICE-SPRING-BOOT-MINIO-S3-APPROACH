package com.fileuploader.upload.controllers;

import com.fileuploader.upload.dto.FileMetadataResponse;
import com.fileuploader.upload.exceptions.ResourceNotFoundException;
import com.fileuploader.upload.services.FileService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(FileController.class)
@AutoConfigureMockMvc(addFilters = false)
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileService fileService;

    @Test
    @WithMockUser(username = "user@example.com")
    void uploadFile_returnsCreatedMetadata() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());
        UUID id = UUID.randomUUID();
        when(fileService.uploadFile(any(), eq("notes.txt"), eq("text/plain"), anyLong(), eq("user@example.com")))
                .thenReturn(new FileMetadataResponse(id, "notes.txt", "text/plain", 5L, "user@example.com"));

        mockMvc.perform(multipart("/api/v1/files").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.fileName").value("notes.txt"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getMyFiles_returnsFilesForAuthenticatedUser() throws Exception {
        UUID id = UUID.randomUUID();
        when(fileService.getFilesForUser("user@example.com"))
                .thenReturn(List.of(new FileMetadataResponse(id, "notes.txt", "text/plain", 5L, "user@example.com")));

        mockMvc.perform(get("/api/v1/files"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].fileName").value("notes.txt"));
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void getFileMetadata_returns404WhenNotOwned() throws Exception {
        UUID id = UUID.randomUUID();
        when(fileService.getFileMetadata(eq(id), eq("user@example.com")))
                .thenThrow(new ResourceNotFoundException("No file found with id " + id));

        mockMvc.perform(get("/api/v1/files/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user@example.com")
    void deleteFile_returnsNoContent() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/files/{id}", id))
                .andExpect(status().isNoContent());
    }
}
