package com.botGithub.bot.service;

import com.botGithub.bot.dto.CommitInfo;
import com.botGithub.bot.model.GithubCommitResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

// ✅ Correto — HttpHeaders do Spring
import org.springframework.http.HttpHeaders;

@Service
public class GithubApiService {
    private final RestTemplate restTemplate;

    @Value("${bot.github.owner}")
    private String owner;

    @Value("${bot.github.repo}")
    private String repo;

    @Value("${bot.github.token}")
    private String token;

    public GithubApiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public CommitInfo getLastCommitSha() {
        String url = "https://api.github.com/repos/" + owner + "/" + repo + "/commits";
        //header de authenticacao
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);// seto a autorizacao do token criado pelo github
        headers.set("Accept", "application/vnd.github+json"); //aceito os terminos da aplicacao com json

        // (6) HttpEntity empacota os headers (sem corpo — é um GET)
        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<GithubCommitResponse[]> response = restTemplate.exchange(url, HttpMethod.GET, request, GithubCommitResponse[].class);

        GithubCommitResponse[] commits = response.getBody();

        String authorname = "Desconhecido";
        String message = "sem mensagem";
        String date = "";
        if (commits == null || commits.length == 0) {
            throw new RuntimeException("Github commit response is null");
        }

        GithubCommitResponse lastes = commits[0];
        if(lastes.commit() != null) {
            message = lastes.commit().message();

            if (lastes.commit().autor() != null) {
                authorname = lastes.commit().autor().name();
                date = lastes.commit().autor().date();
            }
        }
        // (9) commits[0] = mais recente, .sha() sem "get" porque é Record
        return new CommitInfo(lastes.sha(), authorname, date, message);
    }


}
