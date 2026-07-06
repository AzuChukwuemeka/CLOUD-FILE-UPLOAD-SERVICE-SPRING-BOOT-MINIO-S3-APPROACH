package com.fileuploader.upload.services;

import com.fileuploader.upload.dto.UserRegistrationRequest;
import com.fileuploader.upload.dto.UserResponse;
import com.fileuploader.upload.entities.AppUser;
import com.fileuploader.upload.exceptions.ResourceNotFoundException;
import com.fileuploader.upload.exceptions.UserAlreadyExistsException;
import com.fileuploader.upload.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void createUser_savesEncodedPasswordAndReturnsResponse() {
        UserRegistrationRequest request = new UserRegistrationRequest("new.user@example.com", "supersecret");
        when(userRepository.existsByEmail("new.user@example.com")).thenReturn(false);
        when(passwordEncoder.encode("supersecret")).thenReturn("encoded-password");
        when(userRepository.save(any(AppUser.class))).thenAnswer(invocation -> {
            AppUser toSave = invocation.getArgument(0);
            return new AppUser(toSave.getEmail(), toSave.getPassword());
        });

        UserResponse response = userService.createUser(request);

        assertThat(response.email()).isEqualTo("new.user@example.com");
        verify(userRepository).save(argThat(user -> user.getPassword().equals("encoded-password")));
    }

    @Test
    void createUser_throwsWhenEmailAlreadyRegistered() {
        UserRegistrationRequest request = new UserRegistrationRequest("existing@example.com", "supersecret");
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(UserAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserByEmail_throwsWhenNotFound() {
        when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserByEmail("missing@example.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
