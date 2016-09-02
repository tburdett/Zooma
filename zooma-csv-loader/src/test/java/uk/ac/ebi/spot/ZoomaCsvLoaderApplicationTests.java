package uk.ac.ebi.spot;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import uk.ac.ebi.spot.zooma.config.CSVTestConfig;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CSVTestConfig.class)
public class ZoomaCsvLoaderApplicationTests {

	@Test
	public void contextLoads() {
	}

}
