package ds.together.pw.subscriptionmanajer;

import ds.together.pw.subscriptionmanajer.config.SubscriptionProperties;
import ds.together.pw.subscriptionmanajer.service.SubscriptionService;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SubscriptionManajerApplicationTests {

	@Resource
	private SubscriptionProperties subscriptionProperties;
	@Resource
	private SubscriptionService subscriptionService;

	@Test
	void contextLoads() {
	}

}
