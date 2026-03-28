package com.botGithub.bot.service;

import com.botGithub.bot.dto.CommitInfo;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class GithubPollingService {
    private final GithubApiService githubApiService;
    private final GitStateService gitStateService;
    private final MailService mailService;

    public GithubPollingService(GitStateService gitStateService, GithubApiService githubApiService, MailService mailService)  {
        this.gitStateService = gitStateService;
        this.githubApiService = githubApiService;
        this.mailService = mailService;
    }

    @Scheduled(fixedDelayString = "${bot.poll.interval}" ) //ele define o tempo e executa quando chegar nesse tempo
    public void checkForNewCommits() {
        System.out.println("Checking for New Commits... ");

        CommitInfo info = githubApiService.getLastCommitSha();

        if (gitStateService.isNewCommit(info.sha())) {
            System.out.println("New commit found: " + info.sha());

            // atualizar o status do commit
            gitStateService.updateLastSeenCommit(info.sha());

            mailService.sendPullNotification(info);
        } else  {
            System.out.println("New commit not found: ");
        }
    }
}
