package ds.together.pw.subscriptionmanajer.repository;

import ds.together.pw.subscriptionmanajer.entity.po.MaCache;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

@Component
public interface MaCacheRepository extends ReactiveCrudRepository<MaCache, String> {

    Mono<MaCache> findByKey(String key);
}
