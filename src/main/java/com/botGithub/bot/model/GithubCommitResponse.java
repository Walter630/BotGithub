package com.botGithub.bot.model;

public record GithubCommitResponse(String sha, Commit commit) {
    // Record interno representa o objeto "commit" do JSON
    public record Commit(String message, Author autor) {
        // Record interno representa o objeto "author" do JSON
        public record Author(String name, String date) {}
    }
}
