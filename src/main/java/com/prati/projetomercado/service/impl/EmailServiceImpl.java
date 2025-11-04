package com.prati.projetomercado.service.impl;

import com.prati.projetomercado.entity.AuthUser;
import com.prati.projetomercado.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    // O Spring injeta automaticamente o 'JavaMailSender' que configuramos no application.properties
    private final JavaMailSender mailSender;

    // Pega o email do remetente do application.properties para não deixar no código
    @Value("${email.sender.from}")
    private String fromEmail;

    @Override
    public void sendConfirmationEmail(AuthUser user) {
        // O link que o usuário vai clicar. Aponta para o nosso endpoint de confirmação.
        String confirmationUrl = "http://localhost:8080/auth/confirm-registration?token=" + user.getConfirmationToken();

        // Monta a mensagem do e-mail
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Confirme seu Cadastro - Projeto Mercado");
        message.setText("Obrigado por se registrar! Por favor, clique no link abaixo para ativar sua conta:\n" + confirmationUrl);

        // Envia o e-mail
        mailSender.send(message);
    }

    @Override
    public void sendResetCodeEmail(AuthUser user, String code) {
        String text = String.format(
                "Olá,\n\nRecebemos uma solicitação para redefinir sua senha.\n" +
                        "Aqui está o seu código de recuperação:\n\n%s\n\n" +
                        "Este código expira em 15 minutos.\n\n" +
                        "Se você não solicitou esta alteração, pode ignorar este e-mail.",
                code
        );

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(user.getEmail());
        message.setSubject("Redefinição de Senha - Projeto Mercado");
        message.setText(text);

        mailSender.send(message);
    }
}
