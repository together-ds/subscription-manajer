package ds.together.pw.subscriptionmanajer.config;

import ds.together.pw.subscriptionmanajer.entity.ProxyGroup;
import ds.together.pw.subscriptionmanajer.entity.Subscription;
import ds.together.pw.subscriptionmanajer.entity.SubscriptionResult;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.aot.hint.annotation.RegisterReflectionForBinding;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;

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
@ImportRuntimeHints(MigrationRuntimeHints.class)
public class NativeImageConfiguration {
}
class MigrationRuntimeHints implements RuntimeHintsRegistrar {
	@Override
	public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
		hints.resources().registerPattern("db/migration/*.sql");
	}
}
