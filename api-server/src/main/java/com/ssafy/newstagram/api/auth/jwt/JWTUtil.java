package com.ssafy.newstagram.api.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JWTUtil {
    private final SecretKey SECRET_KEY;
    private final long ACCESS_TOKEN_TTL;
    private final long REFRESH_TOKEN_TTL;

    public JWTUtil(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration}") long expiration,
            @Value("${jwt.refresh-expiration}") long refreshExpiration
    ) {
        this.SECRET_KEY = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), Jwts.SIG.HS256.key().build().getAlgorithm());
        this.ACCESS_TOKEN_TTL = expiration;
        this.REFRESH_TOKEN_TTL = refreshExpiration;
    }

    public Long getUserId(String token) {
        return Long.parseLong(parseToken(token).getSubject());
    }

    public String getRole(String token) {
        return parseToken(token).get("role", String.class);
    }

    public String getType(String token){
        return parseToken(token).get("type", String.class);
    }

    public Boolean isExpired(String token) {
        return parseToken(token).getExpiration().before(new Date(System.currentTimeMillis()));
    }

    public String createAccessToken(Long userId, String role) {
        Date issuedDate = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(issuedDate.getTime() + ACCESS_TOKEN_TTL);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)
                .issuedAt(issuedDate)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public String createRefreshToken(Long userId){
        Date issuedDate = new Date(System.currentTimeMillis());
        Date expiryDate = new Date(issuedDate.getTime() + REFRESH_TOKEN_TTL);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", "refresh")
                .issuedAt(issuedDate)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    private Claims parseToken(String token){
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}
