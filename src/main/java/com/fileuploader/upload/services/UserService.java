package com.fileuploader.upload.services;

import com.fileuploader.upload.dto.UserRegistrationRequest;
import com.fileuploader.upload.dto.UserResponse;
import com.fileuploader.upload.entities.AppUser;
import com.fileuploader.upload.exceptions.ResourceNotFoundException;
import com.fileuploader.upload.exceptions.UserAlreadyExistsException;
import com.fileuploader.upload.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public UserResponse createUser(UserRegistrationRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new UserAlreadyExistsException("A user with email " + request.email() + " already exists");
        }
        AppUser saved = userRepository.save(new AppUser(request.email(), passwordEncoder.encode(request.password())));
        return new UserResponse(saved.getId(), saved.getEmail());
    }

    public UserResponse getUserByEmail(String email) {
        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No user found with email " + email));
        return new UserResponse(user.getId(), user.getEmail());
    }
}
