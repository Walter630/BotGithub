package com.botGithub.bot.service;

import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

@Service
public class GitStateService {
    private static final String STATE_FILE="last_commit.txt";

    private String lastSeenSha;

    public GitStateService() {
        this.lastSeenSha = loadShaFromFile();
    }

    public Boolean isNewCommit(String currentCommit) {
        return !currentCommit.equals(lastSeenSha); //retorna false ou true, se houver novo commit ele valida aq
    }

    public void updateLastSeenCommit(String sha) {
        this.lastSeenSha = sha; // é o hash de seguranca de algoritimo
        saveShaToFile(sha);
    }

    private String loadShaFromFile() {
        try{
            Path path = Path.of(STATE_FILE);

            if(!Files.exists(path)) {
                System.out.printf("%s does not exist!%n", STATE_FILE);
                return null;
            }
            String sha = Files.readString(path).trim();
            System.out.println("Sha carregado do arquivo: " + sha);
            return sha;
        } catch (Exception ex) {
            System.err.println("Erro ao ler arquivo de estado: " + ex.getMessage());
            return null;
        }
    }

    private void saveShaToFile(String sha) {
        try {
            Files.writeString(
                    Path.of(STATE_FILE), //
                    sha,
                    StandardOpenOption.CREATE, // cria o arquivo se nao exiter
                    StandardOpenOption.TRUNCATE_EXISTING // sobrescreve o arquivo se existir
            );

            System.out.println("Arquivo criado com sucesso! sha: " + sha);
        }  catch (Exception ex) {
            System.err.println("Erro ao salvar arquivo de estado: " + ex.getMessage());
        }
    }
}
