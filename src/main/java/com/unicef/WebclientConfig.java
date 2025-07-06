package com.unicef;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebclientConfig {

    @Bean
    public WebClient vkWebClient(){
        return WebClient.builder().baseUrl("https://api.vk.com/method").build();
    }

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
