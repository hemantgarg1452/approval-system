package com.company.approval_system;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.mysql.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class ApprovalSystemApplicationTests {

	@Container
	@ServiceConnection
	static MySQLContainer mysql =
			new MySQLContainer("mysql:8.0");

	@Test
	void contextLoads() {
	}

}
