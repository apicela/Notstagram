package apicela.notstagram.services;

import apicela.notstagram.models.Mail;
import lombok.extern.log4j.Log4j2;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class EmailService {
    private final JavaMailSender javaMailSender;

    public EmailService(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void sendMail(Mail mail) {
        var message = new SimpleMailMessage();
        message.setFrom("trab.jamilsouza@gmail.com");
        message.setTo(mail.to());
        message.setSubject(mail.title());
        message.setText(mail.message());
        javaMailSender.send(message);
    }

}