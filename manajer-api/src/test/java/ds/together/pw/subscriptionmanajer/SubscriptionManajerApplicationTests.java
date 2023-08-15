package ds.together.pw.subscriptionmanajer;

import ds.together.pw.subscriptionmanajer.config.SubscriptionProperties;
import ds.together.pw.subscriptionmanajer.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class SubscriptionManajerApplicationTests {

	@Autowired
	private SubscriptionProperties subscriptionProperties;
	@Autowired
	private SubscriptionService subscriptionService;

	@Test
	void contextLoads() {
	}

}
