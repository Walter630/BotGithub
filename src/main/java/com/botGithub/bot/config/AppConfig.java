package com.botGithub.bot.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    @Bean // qualquer classe pode chamar esse campo com new RestTemplate
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
