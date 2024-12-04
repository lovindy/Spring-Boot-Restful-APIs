package com.example.springrestful.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.util.WebUtils;

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

    // Cookie configuration constants
    private static final String ACCESS_TOKEN_COOKIE_NAME = "access_token";
    private static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    private static final boolean USE_HTTP_ONLY = true;
    private static final boolean USE_SECURE = true;
    private static final String COOKIE_PATH = "/";

    private final RedisTemplate<String, String> redisTemplate;

    public JwtUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
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

    // Method to extract token from either Authorization header or Cookie
    public String extractTokenFromRequest(HttpServletRequest request) {
        // First, try to extract from Authorization header
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        // If not found in header, try to extract from cookies
        Cookie tokenCookie = WebUtils.getCookie(request, ACCESS_TOKEN_COOKIE_NAME);
        if (tokenCookie != null && StringUtils.hasText(tokenCookie.getValue())) {
            return tokenCookie.getValue();
        }

        return null;
    }

    // New method to set access token in HTTP-only cookie
    public void setAccessTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, token);
        cookie.setHttpOnly(USE_HTTP_ONLY);
        cookie.setSecure(USE_SECURE);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge((int) (accessTokenExpiration / 1000)); // Convert milliseconds to seconds
        response.addCookie(cookie);
    }

    // New method to set refresh token in HTTP-only cookie
    public void setRefreshTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, token);
        cookie.setHttpOnly(USE_HTTP_ONLY);
        cookie.setSecure(USE_SECURE);
        cookie.setPath(COOKIE_PATH);
        cookie.setMaxAge((int) (refreshTokenExpiration / 1000)); // Convert milliseconds to seconds
        response.addCookie(cookie);
    }

    // Method to clear authentication cookies
    public void clearAuthenticationCookies(HttpServletResponse response) {
        Cookie accessTokenCookie = new Cookie(ACCESS_TOKEN_COOKIE_NAME, null);
        accessTokenCookie.setMaxAge(0);
        accessTokenCookie.setPath(COOKIE_PATH);

        Cookie refreshTokenCookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);
        refreshTokenCookie.setMaxAge(0);
        refreshTokenCookie.setPath(COOKIE_PATH);

        response.addCookie(accessTokenCookie);
        response.addCookie(refreshTokenCookie);
    }

    // Overloaded methods to set tokens in cookies with response parameter
    public String generateToken(UserDetails userDetails, HttpServletResponse response) {
        Map<String, Object> claims = new HashMap<>();
        String token = createToken(claims, userDetails.getUsername(), accessTokenExpiration);
        storeUserSession(userDetails.getUsername(), token, accessTokenExpiration);

        // Set the token in a cookie
        setAccessTokenCookie(response, token);

        return token;
    }

    public String generateRefreshToken(UserDetails userDetails, HttpServletResponse response) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        String token = createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
        storeUserSession(userDetails.getUsername(), token, refreshTokenExpiration);

        // Set the refresh token in a cookie
        setRefreshTokenCookie(response, token);

        return token;
    }

    // Original methods to maintain backward compatibility
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, userDetails.getUsername(), accessTokenExpiration);
    }

    public String generateRefreshToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("tokenType", "refresh");
        return createToken(claims, userDetails.getUsername(), refreshTokenExpiration);
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