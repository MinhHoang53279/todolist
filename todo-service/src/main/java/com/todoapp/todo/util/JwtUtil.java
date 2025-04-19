package com.todoapp.todo.util; // Correct package for todo-service

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct; 
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    // Make sure these properties exist in todo-service/application.yml
    @Value("${jwt.secret:ThisIsAReallyLongAndSecureSecretKeyForJWTEncodingDecodingPleaseChangeThis}") // Default value added
    private String secret;

    @Value("${jwt.expiration:86400000}") // Default value added (24 hours)
    private Long expiration;

    private Key key;

    @PostConstruct
    public void init(){
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Optional: Add generateToken if needed for other purposes in todo-service
    // public String generateToken(String username) {
    //     Map<String, Object> claims = new HashMap<>();
    //     return createToken(claims, username);
    // }

    // private String createToken(Map<String, Object> claims, String subject) {
    //     Date now = new Date();
    //     Date expirationDate = new Date(now.getTime() + expiration);

    //     return Jwts.builder()
    //             .setClaims(claims)
    //             .setSubject(subject)
    //             .setIssuedAt(now)
    //             .setExpiration(expirationDate)
    //             .signWith(key, SignatureAlgorithm.HS256)
    //             .compact();
    // }

    // Optional: Add token validation method used by the filter
    // public Boolean validateToken(String token, String username) {
    //     final String extractedUsername = extractUsername(token);
    //     return (extractedUsername.equals(username) && !isTokenExpired(token));
    // }
} 