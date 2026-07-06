package com.fileuploader.upload.repositories;

import com.fileuploader.upload.entities.FileMetadata;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class FileMetadataRepositoryTest {

    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Test
    void findByUploadedBy_returnsOnlyThatUsersFiles() {
        fileMetadataRepository.save(new FileMetadata(UUID.randomUUID(), "a.txt", "stored-a.txt", "text/plain", 3L, "alice@example.com"));
        fileMetadataRepository.save(new FileMetadata(UUID.randomUUID(), "b.txt", "stored-b.txt", "text/plain", 4L, "bob@example.com"));

        List<FileMetadata> aliceFiles = fileMetadataRepository.findByUploadedBy("alice@example.com");

        assertThat(aliceFiles).hasSize(1);
        assertThat(aliceFiles.get(0).getFileName()).isEqualTo("a.txt");
    }
}
