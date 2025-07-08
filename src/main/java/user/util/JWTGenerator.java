package user.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;

@Component
@Slf4j
public class JWTGenerator {

    @Value("${jwt-expiry-ttl}")
    private long jwtExpiryTtl;
    private static final String secret = "Secret123@@321terceSSecret123@@321terceS@321terceS";

    // Option 1: Option 1: Using Keys.hmacShaKeyFor (recommended for fixed-length secrets)
    SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    // Option 2: Using Keys.secretKeyFor (generates a random secret key, good for dynamic secrets)
    //private static final SecretKey key = Jwts.SIG.HS512.key().build();

    public String generateToken(Authentication authentication) {
        String username = authentication.getName();
        long nowMillis = System.currentTimeMillis();
        System.out.println("jwtExpiryTtl : " + jwtExpiryTtl);
        long ttlMillis = nowMillis + jwtExpiryTtl;
        log.info("JWT Info - [nowMillis: {}, jwtExpiryTtl: {} ttlMillis: {}]", nowMillis, jwtExpiryTtl, ttlMillis);

        Date currentDate = new Date(nowMillis);
        Date expireDate = new Date(ttlMillis);

        String token = Jwts.builder()
                .subject(username)
                .issuedAt(currentDate)
                .expiration(expireDate)
                .signWith(key)
                .compact();
        log.debug("Generated JWT token : {}", token);
        return token;
    }

    public String getUsernameFromJWT(String token){
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token).getPayload();
            return true;
        } catch (Exception ex) {
            throw new AuthenticationCredentialsNotFoundException("JWT is either expired or incorrect",ex.fillInStackTrace());
        }
    }
}