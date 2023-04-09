package ds.together.pw.subscriptionmanajer.config;

import ds.together.pw.subscriptionmanajer.entity.Subscription;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zkq
 * @version 1.0
 * @date 2023/4/9 20:44
 */
@Component
@ConfigurationProperties(prefix = "manajer")
public class SubscriptionProperties {
    private List<Subscription> subscriptions;

    public List<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(List<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
