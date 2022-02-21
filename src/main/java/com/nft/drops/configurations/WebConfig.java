package com.nft.drops.configurations;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
public class WebConfig {

    @Bean
    public RestTemplate restTemplate() {
        Duration duration = Duration.ofSeconds(15);
        return new RestTemplateBuilder()
                .setConnectTimeout(duration)
                .setReadTimeout(duration)
                .build();
    }

}
