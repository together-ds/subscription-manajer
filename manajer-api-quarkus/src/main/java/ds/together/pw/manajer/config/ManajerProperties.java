package ds.together.pw.manajer.config;

import io.smallrye.config.ConfigMapping;

import java.util.List;

/**
 * @author x
 * @version 1.0
 * @since 2023/4/9 20:44
 */
@ConfigMapping(prefix = "manajer")
public interface ManajerProperties {

	List<Subscription> subscriptions();

	String token();

	interface Subscription {
		String name();

		String url();

	}
}
