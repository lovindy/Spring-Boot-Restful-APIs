package com.example.springrestful.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.redis.prefix.blacklist}")
    private String blacklistPrefix;

    @Value("${jwt.redis.prefix.user-sessions}")
    private String userSessionsPrefix;

    private final RedisTemplate<String, String> redisTemplate;

    public JwtUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String extractTokenFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
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
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername(), accessTokenExpiration);
        storeUserSession(userDetails.getUsername(), token, accessTokenExpiration);
        return token;
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        String token = createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
        storeUserSession(userDetails.getUsername(), token, refreshTokenExpiration);
        return token;
    }

    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private void storeUserSession(String username, String token, long expiration) {
        String sessionKey = userSessionsPrefix + username;
        redisTemplate.opsForSet().add(sessionKey, token);
        redisTemplate.expire(sessionKey, expiration, TimeUnit.MILLISECONDS);
    }

    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token) &&
                !isTokenBlacklisted(token) &&
                isValidUserSession(username, token));
    }

    private boolean isValidUserSession(String username, String token) {
        String sessionKey = userSessionsPrefix + username;
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(sessionKey, token));
    }

    public void invalidateToken(String token) {
        if (token == null) return;

        String username = extractUsername(token);
        Date expiration = extractExpiration(token);
        long remainingTtl = Math.max((expiration.getTime() - System.currentTimeMillis()), 0);

        // Add to blacklist
        String blacklistKey = blacklistPrefix + token;
        redisTemplate.opsForValue().set(blacklistKey, "true", remainingTtl, TimeUnit.MILLISECONDS);

        // Remove from user sessions
        String sessionKey = userSessionsPrefix + username;
        redisTemplate.opsForSet().remove(sessionKey, token);
    }

    public void invalidateAllUserSessions(String username) {
        String sessionKey = userSessionsPrefix + username;
        redisTemplate.delete(sessionKey);
    }

    public boolean isTokenBlacklisted(String token) {
        if (token == null) return false;
        String blacklistKey = blacklistPrefix + token;
        return Boolean.TRUE.toString().equals(redisTemplate.opsForValue().get(blacklistKey));
    }
}