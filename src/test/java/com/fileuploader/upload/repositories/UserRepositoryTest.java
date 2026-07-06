package com.fileuploader.upload.repositories;

import com.fileuploader.upload.entities.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByEmail_isTrueOnlyAfterSaving() {
        assertThat(userRepository.existsByEmail("new.user@example.com")).isFalse();

        userRepository.save(new AppUser("new.user@example.com", "encoded-password"));

        assertThat(userRepository.existsByEmail("new.user@example.com")).isTrue();
        assertThat(userRepository.findByEmail("new.user@example.com")).isPresent();
    }
}
