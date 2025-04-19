package com.todoapp.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    private Key key;

    @PostConstruct
    public void init() {
        // Đảm bảo secret đủ dài
        if (secret == null || secret.getBytes().length * 8 < 256) {
            log.error("JWT Secret is not configured or too short (must be at least 256 bits)");
            // Có thể throw exception ở đây để dừng khởi động nếu secret không an toàn
             throw new IllegalArgumentException("JWT Secret is not configured or too short");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        log.info("JWT Key initialized successfully.");
    }

    public String extractUsername(String token) {
        try {
            return extractClaim(token, Claims::getSubject);
        } catch (Exception e) {
            log.warn("Could not extract username from token: {}", e.getMessage());
            return null; // Trả về null nếu không giải mã được
        }
    }

    public Date extractExpiration(String token) {
        try {
             return extractClaim(token, Claims::getExpiration);
        } catch (Exception e) {
             log.warn("Could not extract expiration from token: {}", e.getMessage());
             return null;
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        // Thêm xử lý lỗi cụ thể hơn
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
             log.error("Invalid JWT token: {}", e.getMessage());
             throw e;
        }
    }

    private Boolean isTokenExpired(String token) {
        Date expirationDate = extractExpiration(token);
        return expirationDate != null && expirationDate.before(new Date());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        // Có thể thêm các claims khác vào đây nếu cần (ví dụ: roles)
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + expiration);

        log.debug("Creating token for {} expiring at {}", subject, expirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expirationDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // Có thể thêm hàm validate đầy đủ hơn nếu cần
    // public Boolean validateToken(String token, String expectedUsername) {
    //     final String username = extractUsername(token);
    //     return (username != null && username.equals(expectedUsername) && !isTokenExpired(token));
    // }
} 