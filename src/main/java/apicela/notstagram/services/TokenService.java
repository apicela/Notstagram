package apicela.notstagram.services;

import apicela.notstagram.models.entities.User;
import apicela.notstagram.utils.DateUtils;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;

@Service
public class TokenService {
    private final String SECRET = "urkewirjiewrjwierhweiurh4124821u83eh2e21";

    private final Key key = Keys.hmacShaKeyFor(SECRET.getBytes());

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", user.getAuthorities())
                .setIssuedAt(new Date())
                .setExpiration(DateUtils.minutesFromNow(60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String generateTokenWithExtraAuthority(User user, String extraAuthority) {
        return Jwts.builder()
                .setSubject(user.getEmail())
                .claim("roles", user.getAuthorities())
                .claim("extra", extraAuthority)
                .setIssuedAt(new Date())
                .setExpiration(DateUtils.minutesFromNow(60))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String extractEmail(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    public String extractExtraAuthority(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .get("extra", String.class);
    }

    public boolean isTokenValid(String token, User user) {
        return extractEmail(token).equals(user.getEmail())
                && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key).build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration()
                .before(new Date());
    }
}
