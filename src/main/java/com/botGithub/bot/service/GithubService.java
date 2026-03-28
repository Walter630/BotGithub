package com.botGithub.bot.service;


import org.springframework.web.client.RestTemplate;

public class GithubService {
    private final RestTemplate restTemplate;

    public GithubService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
}
