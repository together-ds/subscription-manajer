package ds.together.pw.subscriptionmanajer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SubscriptionManajerApplication implements CommandLineRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(SubscriptionManajerApplication.class);

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(SubscriptionManajerApplication.class);
        application.run(args);
    }

    @Override
    public void run(String... args) {
        LOGGER.info("Hello Manajer!");
    }
}
