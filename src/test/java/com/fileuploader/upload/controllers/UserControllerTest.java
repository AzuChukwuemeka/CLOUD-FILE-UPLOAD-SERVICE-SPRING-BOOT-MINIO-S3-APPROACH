package com.fileuploader.upload.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fileuploader.upload.dto.UserResponse;
import com.fileuploader.upload.exceptions.UserAlreadyExistsException;
import com.fileuploader.upload.services.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Test
    void register_returnsCreatedUser() throws Exception {
        when(userService.createUser(any())).thenReturn(new UserResponse(1L, "new.user@example.com"));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new.user@example.com","password":"supersecret"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("new.user@example.com"));
    }

    @Test
    void register_rejectsInvalidEmail() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"not-an-email","password":"supersecret"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_rejectsShortPassword() throws Exception {
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new.user@example.com","password":"short"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_returnsConflictWhenEmailTaken() throws Exception {
        when(userService.createUser(any())).thenThrow(new UserAlreadyExistsException("A user with email new.user@example.com already exists"));

        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"email":"new.user@example.com","password":"supersecret"}
                                """))
                .andExpect(status().isConflict());
    }
}
