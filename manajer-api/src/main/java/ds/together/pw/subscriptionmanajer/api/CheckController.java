package ds.together.pw.subscriptionmanajer.api;

import ds.together.pw.subscriptionmanajer.web.Result;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * HealthCheck
 * @author x
 * @version 1.0
 */
@RestController
@RequestMapping("${manajer.base-path:}/status")
public class CheckController {

    @GetMapping(value = "/ping")
    public Mono<Result<String>> ping() {
        return Mono.fromCallable(() -> Result.success("pong"));
    }

}
