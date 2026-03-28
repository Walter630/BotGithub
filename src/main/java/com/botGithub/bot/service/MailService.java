package com.botGithub.bot.service;

import com.botGithub.bot.dto.CommitInfo;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class MailService {
    private final JavaMailSender mailSender;

    @Value("${bot.notify-email}")
    private String notifyEmail;

    @Value("${bot.base-url}")
    private String baseUrl;

    private String pedingToken;

    //Construtor da classe
    // construtor manual — só injeta o JavaMailSender
    // os @Value são injetados pelo Spring depois da construção
    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendPullNotification(CommitInfo commitInfo) {
        // (2) gera um token único para essa notificação
        this.pedingToken = UUID.randomUUID().toString();

        // (3) monta os links com o token
        String acceptUrl = baseUrl + "/pull/accept?token=" + pedingToken;
        String rejectUrl = baseUrl + "/pull/reject?token=" + pedingToken;

        // (4) monta o corpo do email em HTML com Text Block do Java 25
        String html = """
            <!DOCTYPE html>
            <html>
              <body style="font-family: Arial, sans-serif; padding: 24px; color: #333;">

                <h2 style="color: #2d6a4f;">🔔 Novo commit detectado!</h2>

                <table style="background:#f4f4f4; padding:16px;
                              border-radius:8px; width:100%%; border-collapse:collapse;">
                  <tr>
                    <td style="padding:6px 12px;"><strong>SHA</strong></td>
                    <td style="font-family:monospace; padding:6px 12px;">%s</td>
                  </tr>
                  <tr style="background:#ebebeb;">
                    <td style="padding:6px 12px;"><strong>Autor</strong></td>
                    <td style="padding:6px 12px;">%s</td>
                  </tr>
                  <tr>
                    <td style="padding:6px 12px;"><strong>Mensagem</strong></td>
                    <td style="padding:6px 12px;">%s</td>
                  </tr>
                  <tr style="background:#ebebeb;">
                    <td style="padding:6px 12px;"><strong>Data</strong></td>
                    <td style="padding:6px 12px;">%s</td>
                  </tr>
                </table>

                <br/>
                <p>Deseja executar o <strong>git pull</strong> no repositório local?</p>

                <a href="%s"
                   style="background:#2d6a4f; color:white; padding:12px 24px;
                          text-decoration:none; border-radius:6px; margin-right:12px;">
                  ✅ Aceitar pull
                </a>

                <a href="%s"
                   style="background:#c1121f; color:white; padding:12px 24px;
                          text-decoration:none; border-radius:6px;">
                  ❌ Recusar
                </a>

                <br/><br/>
                <p style="color:#999; font-size:12px;">
                  Este link expira na próxima verificação do bot.
                </p>

              </body>
            </html>
                """.formatted(commitInfo.sha(), commitInfo.authorName(), commitInfo.message(), commitInfo.date(), acceptUrl, rejectUrl); // (5)

        // (6) envia o email
        send(notifyEmail, "🔔 Novo commit detectado — Git Bot", html);
    }

    public Boolean isValidateToken(String token) {
        return pedingToken != null && pedingToken.equals(token);
    }

    public void invalidateToken() {
        this.pedingToken = null;
    }

    public void send(String to, String message, String html) {
        try{
            MimeMessage messages = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(messages, true, "UTF-8");
            helper.setTo(to);
            helper.setSubject(message);
            helper.setText(html, true);
            mailSender.send(messages);

            System.out.println("Email enviado com sucesso!: " + to);
        } catch (Exception e) {
            System.err.println("Erro ao enviar email: " + e.getMessage());
        }
    }
}
