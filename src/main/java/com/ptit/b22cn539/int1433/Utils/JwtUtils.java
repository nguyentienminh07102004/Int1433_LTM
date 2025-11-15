package com.ptit.b22cn539.int1433.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtUtils {
    @Value(value = "${jwt.secret_key}")
    String secretKey;

    public String generateToken(String username) {
        SecretKey keySpec = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.builder()
                .issuer("ptit-b22cn539")
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(keySpec)
                .compact();
    }

    public Claims extractClaims(String token) {
        SecretKey keySpec = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(keySpec)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
