package ds.together.pw.subscriptionmanajer.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import ds.together.pw.subscriptionmanajer.entity.po.MaCache;
import ds.together.pw.subscriptionmanajer.repository.MaCacheRepository;
import ds.together.pw.subscriptionmanajer.service.MaCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class MaCacheServiceImpl implements MaCacheService {
    @Autowired
    private MaCacheRepository maCacheRepository;
    @Autowired
    private ObjectMapper jsonMapper;

    public static long CACHE_EXPIRED_SECOND = 60 * 15;
    private static final Logger LOGGER = LoggerFactory.getLogger(MaCacheServiceImpl.class);

    @Override
    public <T> Mono<T> load(String key, Mono<T> supplier, TypeReference<T> type) {
        Objects.requireNonNull(supplier, "ma cache loader can not be null.");
        if (key == null) {
            return supplier;
        }

        Mono<MaCache> maCacheById = maCacheRepository.findByKey(key).checkpoint();
        return maCacheById.doOnError(e -> {
                              LOGGER.error(e.getMessage(), e);
                          }).filterWhen(maCache -> {
                              boolean expired = checkExpired(maCache);
                              Mono<Void> empty = Mono.empty();
                              if (expired) {
                                  empty = maCacheRepository.delete(maCache);
                              }
                              return empty.thenReturn(!expired);
                          })
                          .map(maCache -> {
                              try {
                                  return jsonMapper.readValue(maCache.getContent(), type);
                              } catch (JsonProcessingException e) {
                                  throw new RuntimeException(e);
                              }
                          })
                          .switchIfEmpty(supplier.checkpoint().flatMap(t -> {
                              try {
                                  MaCache newCache = new MaCache();
                                  newCache.setKey(key);
                                  newCache.setCreated(LocalDateTime.now());
                                  newCache.setContent(jsonMapper.writeValueAsString(t));
                                  return maCacheRepository.save(newCache).thenReturn(t);
                              } catch (JsonProcessingException e) {
                                  LOGGER.error(e.getMessage(), e);
                              }
                              return Mono.just(t);
                          }));
    }


    /**
     * @param maCache cache data
     * @return if true , the data is expired.
     */
    private boolean checkExpired(MaCache maCache) {
        LocalDateTime time = maCache.getCreated();
        if (time == null) {
            return true;
        }
        LocalDateTime latestValidTime = time.plusSeconds(CACHE_EXPIRED_SECOND);
        return latestValidTime.isBefore(LocalDateTime.now());
    }

}
