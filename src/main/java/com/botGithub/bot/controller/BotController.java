package com.botGithub.bot.controller;

import com.botGithub.bot.service.GitService;
import com.botGithub.bot.service.MailService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/pull")
public class BotController {

    private final GitService gitService;
    private final MailService mailService;

    public BotController(GitService gitService, MailService mailService) {
        this.gitService = gitService;
        this.mailService = mailService;
    }

    @GetMapping("/accept")
    public ResponseEntity<String> accept(@RequestParam String token) {

        // (1) valida o token antes de fazer qualquer coisa
        if (!mailService.isValidateToken(token)) {
            return ResponseEntity
                    .status(403)
                    .body(buildPage("❌ Token inválido ou expirado.", "#c1121f"));
        }

        // (2) invalida o token imediatamente — evita clique duplo
        mailService.invalidateToken();

        // (3) executa o git pull
        GitService.GitPullResult result = gitService.executePull();

        // (4) retorna uma página HTML com o resultado
        if (result.success()) {
            return ResponseEntity.ok(
                    buildPage("✅ git pull executado com sucesso!", "#2d6a4f")
            );
        } else {
            return ResponseEntity
                    .status(500)
                    .body(buildPage("⚠️ git pull falhou: " + result.output(), "#e76f51"));
        }
    }

    @GetMapping("/reject")
    public ResponseEntity<String> reject(@RequestParam String token) {

        // (5) valida mesmo no reject — segurança
        if (!mailService.isValidateToken(token)) {
            return ResponseEntity
                    .status(403)
                    .body(buildPage("❌ Token inválido ou expirado.", "#c1121f"));
        }

        mailService.invalidateToken();
        System.out.println("🚫 Pull recusado pelo usuário.");

        return ResponseEntity.ok(
                buildPage("🚫 Pull recusado. Nenhuma alteração foi feita.", "#555")
        );
    }

    // (6) monta uma página HTML simples de feedback
    private String buildPage(String message, String color) {
        return """
                <!DOCTYPE html>
                <html>
                  <body style="font-family: Arial, sans-serif; padding: 48px; text-align: center;">
                    <h2 style="color: %s;">%s</h2>
                    <p style="color: #999;">Você já pode fechar esta página.</p>
                  </body>
                </html>
                """.formatted(color, message);
    }
}