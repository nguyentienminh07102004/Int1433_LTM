package com.ptit.b22cn539.int1433.Utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.util.Date;

@Component
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtUtils {
    @Value(value = "${jwt.secret_key}")
    String secretKey;

    public String generateToken(String username) {
        return Jwts.builder()
                .issuer("ptit-b22cn539")
                .subject(username)
                .expiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(new SecretKeySpec(secretKey.getBytes(), "HmacSHA512"))
                .compact();
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(new SecretKeySpec(secretKey.getBytes(), "HmacSHA512"))
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
