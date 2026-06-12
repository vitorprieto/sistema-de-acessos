package com.sistema.acesso.auth_service;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
@Disabled("Integration test: boots datasource + Flyway, needs PostgreSQL. "
    + "Re-enable in the integration phase (Testcontainers).")
class AuthServiceApplicationTests {

	@Test
	void contextLoads() {
	}

}
