package ds.together.pw.subscriptionmanajer.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * @author zkq
 * @version 1.0
 * @date 2023/4/5 15:57
 */

@Configuration
public class WebConfiguration {
    @Bean
    public WebClient getWebClientBuilder(WebClient.Builder builder) {
        return builder.build();
    }

    @Bean
    public ObjectMapper yamlMapper(){
        YAMLFactory jf = new YAMLFactory();
        return new ObjectMapper(jf);
    }

}
