package com.fileuploader.upload;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class UploadApplicationTests {

	@Test
	void contextLoads() {
		// Verifies the full application context (H2 datasource, JPA, security, MinIO client bean,
		// and OpenAPI config) wires up correctly. The MinIO bucket check is disabled for the
		// "test" profile, so this does not require a running MinIO instance.
	}

}
