package ds.together.pw.subscriptionmanajer.service;

import reactor.core.publisher.Mono;

/**
 * @author x
 * @version 1.0
 * @since 2023/4/5 15:48
 */
public interface SubscriptionService {
    Mono<String> get(String token);
}
