package com.example.springrestful.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
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

    // Configuration properties for JWT generation and validation
    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.access-token.expiration}")
    private long accessTokenExpiration;

    @Value("${jwt.refresh-token.expiration}")
    private long refreshTokenExpiration;

    @Value("${jwt.blacklist.prefix}")
    private String blacklistPrefix;

    @Value("${jwt.blacklist.ttl.seconds}")
    private long blacklistTtlSeconds;

    // Redis template for token blacklisting
    private final RedisTemplate<String, String> redisTemplate;

    public JwtUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // Extract token from Authorization header
    public String extractTokenFromRequest(HttpServletRequest request) {
        // Check for valid Bearer token
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // Create a secure signing key using the secret
    private SecretKey getSigningKey() {
        // Convert secret to bytes and generate HMAC SHA key
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Extract specific claims from the token
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // Generic method to extract any claim from the token
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parse and extract all claims from the token
    private Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Check if the token has expired
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // Generate access token for a user
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    // Generate refresh token for a user
    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
    }

    // Core token creation method
    private String createToken(Map<String, Object> claims, String subject, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Comprehensive token validation
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) &&
                !isTokenExpired(token) &&
                !isTokenBlacklisted(token));
    }

    // Invalidate a token by adding it to the Redis blacklist
    public void invalidateToken(String token) {
        Date expiration = extractExpiration(token);
        long remainingTtl = Math.max((expiration.getTime() - System.currentTimeMillis()) / 1000, 0);

        // Use the configured TTL, but don't exceed the token's remaining lifetime
        long finalTtl = Math.min(remainingTtl, blacklistTtlSeconds);

        // Blacklist the token in Redis with its original TTL
        if (finalTtl > 0) {
            String blacklistKey = blacklistPrefix + token;
            redisTemplate.opsForValue().set(blacklistKey, "true", finalTtl, TimeUnit.SECONDS);
        }
    }


    // Check if a token is blacklisted in Redis
    public boolean isTokenBlacklisted(String token) {
        if (token == null) return false;
        String blacklistKey = blacklistPrefix + token;
        return Boolean.TRUE.toString().equals(redisTemplate.opsForValue().get(blacklistKey));
    }
}