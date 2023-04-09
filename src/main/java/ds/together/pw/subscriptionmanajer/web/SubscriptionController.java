package ds.together.pw.subscriptionmanajer.web;

import ds.together.pw.subscriptionmanajer.service.SubscriptionService;
import jakarta.annotation.Resource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author zkq
 * @version 1.0
 * @date 2023/4/5 15:39
 */

@RestController
@RequestMapping("/subscribe")
public class SubscriptionController {
    @Resource
    private SubscriptionService subscriptionService;


    @GetMapping(value = "/get-template", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> getTemplate() throws IOException {
        ClassPathResource resource = new ClassPathResource("template.yaml");
        String template = resource.getContentAsString(StandardCharsets.UTF_8);
        return Mono.just(template);
    }


    @GetMapping(value = "/get", produces = MediaType.TEXT_PLAIN_VALUE)
    public Mono<String> get() {

        return subscriptionService.get();
    }

}
