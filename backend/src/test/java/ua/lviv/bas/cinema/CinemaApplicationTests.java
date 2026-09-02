package ua.lviv.bas.cinema;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import ua.lviv.bas.cinema.config.TestcontainersConfig;

@SpringBootTest
@ActiveProfiles("ci")
@Import(TestcontainersConfig.class)
class CinemaApplicationTests {

	@Test
	void contextLoads() {
	}

}
