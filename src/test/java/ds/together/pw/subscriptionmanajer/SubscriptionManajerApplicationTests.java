package ds.together.pw.subscriptionmanajer;

import ds.together.pw.subscriptionmanajer.config.SubscriptionProperties;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SubscriptionManajerApplicationTests {

	@Resource
	private SubscriptionProperties subscriptionProperties;

	@Test
	void contextLoads() {
	}

}
