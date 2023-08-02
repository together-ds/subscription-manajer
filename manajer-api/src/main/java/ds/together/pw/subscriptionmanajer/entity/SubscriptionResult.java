package ds.together.pw.subscriptionmanajer.entity;

import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * @author x
 * @version 1.0
 * @since 2023/4/5 18:51
 */
public class SubscriptionResult {
    private final Subscription subscription;
    private ArrayNode proxies;

    public SubscriptionResult(Subscription subscription) {
        this.subscription = subscription;
    }


    public ArrayNode getProxies() {
        return proxies;
    }

    public void setProxies(ArrayNode proxies) {
        this.proxies = proxies;
    }

    public Subscription getSubscription() {
        return subscription;
    }
}
