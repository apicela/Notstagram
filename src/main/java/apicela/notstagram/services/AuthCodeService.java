package apicela.notstagram.services;

import apicela.notstagram.models.Mail;
import apicela.notstagram.models.entities.AuthCode;
import apicela.notstagram.models.entities.User;
import apicela.notstagram.repositories.AuthCodeRepository;
import apicela.notstagram.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Optional;

@Service
public class AuthCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final AuthCodeRepository authCodeRepository;
    private final EmailService emailService;

    public AuthCodeService(AuthCodeRepository authCodeRepository, EmailService emailService) {
        this.authCodeRepository = authCodeRepository;
        this.emailService = emailService;
    }

    public void generateAuthCodeAndSendEmail(User u) {
        AuthCode authCode = getAuthCodeFromUser(u);
  //      sendMail(u, authCode);
    }

    public AuthCode getAuthCodeFromUser(User user) {
        Optional<AuthCode> authCode = authCodeRepository.findLastValidByUser(user, DateUtils.minutesFromNowLocal(15));
        if (authCode.isPresent()) {
            return authCode.get();
        } else {
            AuthCode newAuthCode = new AuthCode();
            newAuthCode.setCode(generate6DigitsCode());
            newAuthCode.setExpiration(DateUtils.minutesFromNowLocal(60));
            newAuthCode.setUser(user);
            return authCodeRepository.save(newAuthCode);
        }
    }

    public void sendMail(User user, AuthCode authCode) {
        String msg = "Olá, seu código é: " + authCode.getCode();
        Mail m = new Mail(user.getEmail(), "Código de verificação - Notstagram", msg);
        emailService.sendMail(m);
    }

    private int generate6DigitsCode() {
        return 100000 + RANDOM.nextInt(900000);
    }
}
