package ds.together.pw.subscriptionmanajer.service;

import reactor.core.publisher.Mono;

/**
 * @author zkq
 * @version 1.0
 * @date 2023/4/5 15:48
 */
public interface SubscriptionService {
    Mono<String> get();
}
