package ds.together.pw.subscriptionmanajer.service;

import com.fasterxml.jackson.core.type.TypeReference;
import reactor.core.publisher.Mono;

public interface MaCacheService {


    <T> Mono<T> load(String key, Mono<T> supplier, TypeReference<T> type);
}
