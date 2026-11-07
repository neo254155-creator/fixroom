package dev.fixroom;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "fixroom.data-dir=target/context-test-data")
class FixRoomApplicationTests {

	@Test
	void contextLoads() {
	}

}
