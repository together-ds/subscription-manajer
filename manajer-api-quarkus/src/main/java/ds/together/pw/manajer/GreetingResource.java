package ds.together.pw.manajer;

import ds.together.pw.manajer.config.ManajerProperties;
import ds.together.pw.manajer.entity.po.MaCache;
import ds.together.pw.manajer.repository.MaCacheRepository;
import ds.together.pw.manajer.web.Result;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Path("/hello")
public class GreetingResource {

    @Inject
    MaCacheRepository maCacheRepository;
    @Inject
    ManajerProperties manajerProperties;

/*
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from RESTEasy Reactive";
    }
*/

    @GET
    @Path("/test")
    public Result<MaCache> test() {

        MaCache data = new MaCache();
        data.setId(RandomUtils.nextInt());
        data.setContent(RandomStringUtils.randomAlphabetic(64));
        data.setKey(RandomStringUtils.randomAlphabetic(8));
        data.setCreated(LocalDateTime.now());
        return Result.success(data);
    }
    @GET
    @Path("/test2")
    public Result<List<MaCache>> test2() {

        List<MaCache> maCaches = maCacheRepository.listAll();
        return Result.success(maCaches);
    }
    @GET
    @Path("/test3")
    public Result<List<HashMap<String, Object>>> test3() {
        List<ManajerProperties.Subscription> subscriptions = manajerProperties.subscriptions();
        List<HashMap<String, Object>> list = subscriptions.stream().map(subscription -> {
            HashMap<String, Object> map = new HashMap<>();
            map.put("name", subscription.name());
            map.put("url", subscription.url());
            return map;
        }).toList();
        return Result.success(list);
    }



}
