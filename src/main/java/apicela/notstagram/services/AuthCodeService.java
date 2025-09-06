package apicela.notstagram.services;

import apicela.notstagram.models.AuthCode;
import apicela.notstagram.utils.DateUtils;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Service
public class AuthCodeService {
    private static final SecureRandom RANDOM = new SecureRandom();

    public void generateAuthCodeAndSendEmail(String token){
        AuthCode authCode = new AuthCode();
        authCode.setCode(generateCode());
        authCode.setExpiration(DateUtils.minutesFromNow(60));
        authCode.setUser();
    }
    private int generateCode() {
        return 100000 + RANDOM.nextInt(900000);
    }
}
