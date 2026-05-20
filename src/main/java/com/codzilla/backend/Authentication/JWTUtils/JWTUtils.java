package com.codzilla.backend.Authentication.JWTUtils;

import com.codzilla.backend.Authentication.config.AuthSettings;
import com.codzilla.backend.User.User;
import io.jsonwebtoken.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Component
public class JWTUtils {

    private static SecretKey secret = Jwts.SIG.HS256.key().build();
    private final AuthSettings settings;

    public JWTUtils(AuthSettings settings) {
        this.settings = settings;
    }

    public String generateAccessToken(User user) {
        List<String> roles = user.getAuthorities().stream()
                                 .map(GrantedAuthority::getAuthority)
                                 .filter(role -> !Objects.equals(
                                         role,
                                         "FACTOR_PASSWORD"
                                 ))
                                 .toList();

        return Jwts.builder()
                   .subject(user.getEmail())
                   .claim(
                           "roles",
                           roles
                   )
                   .claim(
                           "id",
                           user.getId()
                   )
                   .issuedAt(new Date())
                   .expiration(new Date(
                           System.currentTimeMillis() + settings.getAccessTokenTtl().toMillis()))
                   .signWith(secret)
                   .compact();
    }

    public String generateRefreshToken(User user) {
        return Jwts.builder()
                   .subject(user.getEmail())
                   .setId(UUID.randomUUID().toString())
                   .issuedAt(new Date())
                   .expiration(new Date(
                           System.currentTimeMillis() + settings.getRefreshTokenTtl().toMillis()))
                   .signWith(secret)
                   .compact();
    }

    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(secret)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

        return claims.get(
                "roles",
                List.class
        );
    }

    public String getEmailFromToken(String token) {
        return Jwts.parser()
                   .verifyWith(secret)
                   .build()
                   .parseSignedClaims(token)
                   .getPayload()
                   .getSubject();
    }

    public UUID getIdFromToken(String token) {
        Claims claims = Jwts.parser()
                            .verifyWith(secret)
                            .build()
                            .parseSignedClaims(token)
                            .getPayload();

        String stringUUID = claims.get(
                "id",
                String.class
        );
        return UUID.fromString(stringUUID);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }

    }
}

