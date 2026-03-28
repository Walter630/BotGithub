package com.botGithub.bot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

@Service
public class GitService {
    @Value("${bot.repo.path}")
    private String repoPath;

    public GitPullResult executePull(){
        try{
            //processa o comando a ser criado e usado
            ProcessBuilder builder = new ProcessBuilder("git", "pull");
            builder.directory(new File(repoPath));
            builder.redirectErrorStream(true);

            //vai iniciar o processo
            Process process = builder.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;

                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                    System.out.println(line);
                }
            }
            int exitCode = process.waitFor();

            if(exitCode == 0){
                System.out.println("Git pull completed");
                return new GitPullResult(true, output.toString());
            }else {
                System.out.println("Git pull failed");
                return new GitPullResult(false, output.toString());
            }
        } catch (Exception e){
            System.err.println("Git pull failed "+ e.getMessage());
            return new GitPullResult(false, e.getMessage());
        }
    }

    public record GitPullResult(Boolean success, String output){}
}
