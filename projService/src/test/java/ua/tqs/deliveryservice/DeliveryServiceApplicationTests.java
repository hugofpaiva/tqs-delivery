package ua.tqs.deliveryservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@Testcontainers
class DeliveryServiceApplicationTests {

	@Container
	public static PostgreSQLContainer container = new PostgreSQLContainer("postgres:11.12")
			.withUsername("demo")
			.withPassword("demopw")
			.withDatabaseName("delivery");


	@Test
	void contextLoads() {
	}

}
