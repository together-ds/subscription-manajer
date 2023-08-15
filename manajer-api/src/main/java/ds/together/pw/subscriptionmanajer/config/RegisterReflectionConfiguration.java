package ds.together.pw.subscriptionmanajer.config;

import ds.together.pw.subscriptionmanajer.entity.ProxyGroup;
import ds.together.pw.subscriptionmanajer.entity.Subscription;
import ds.together.pw.subscriptionmanajer.entity.SubscriptionResult;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;

/**
 * 需要序列化的类，放到RegisterReflectionForBinding里。否则 native image无法完成序列化
 * @author : x
 * @version : 1.0
 * @since : 2023/8/15
 */

@Configuration
@RegisterReflectionForBinding({
		ProxyGroup.class,
		Subscription.class,
		SubscriptionResult.class
})
public class RegisterReflectionConfiguration {
}
