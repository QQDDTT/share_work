package com.nick.share_work.frame.jwt;

import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import com.nick.share_work.frame.authentication.AuthenticationService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import java.security.Key;
import java.util.Date;

/**
 * JwtTokenProvider 类用于生成和验证 JWT 令牌，以及从令牌中提取认证信息。
 * 
 * @author nick
 */
@Component
public class JwtTokenProvider {

    // 日志记录器，用于记录日志信息
    private static final Logger LOGGER = LoggerFactory.getLogger(JwtTokenProvider.class);

    // 用于签名 JWT 的密钥（使用 HS256 算法）
    @SuppressWarnings("deprecation")
    private static final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    // 令牌的过期时间（1 天，单位为毫秒）
    private static final long EXPIRATION_TIME = 86400000;

    // 用户服务，用于加载用户信息
    private final AuthenticationService authenticationService;

    /**
     * 构造方法，注入 UserServiceImpl。
     *
     * @param userService 用户服务实例
     */
    public JwtTokenProvider(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    /**
     * 生成 JWT 令牌。
     *
     * @param authentication 认证信息（通常包含用户身份）
     * @return 生成的 JWT 令牌字符串
     */
    @SuppressWarnings("deprecation")
    public String generateToken(Authentication authentication) {
        LOGGER.debug("Create JWT token for authentication: {}", authentication);
        // 从认证信息中获取用户名
        String username = authentication.getName();
        // 获取当前时间作为令牌签发时间
        Date now = new Date();
        // 设置令牌的过期时间
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        // 构建并返回 JWT 令牌字符串
        String token = Jwts.builder()
                .setSubject(username) // 设置令牌的主题（通常为用户名）
                .setIssuedAt(now) // 设置令牌的签发时间
                .setExpiration(expiryDate) // 设置令牌的过期时间
                .signWith(key) // 使用密钥签名令牌
                .compact(); // 构建并压缩成 JWT 字符串
        LOGGER.debug("Generated JWT token: {}", token);
        return token;
    }

    /**
     * 从 JWT 令牌中获取认证信息。
     *
     * @param token JWT 令牌字符串
     * @return 包含用户信息的认证对象，如果解析失败则返回 Mono.empty()
     */
    @SuppressWarnings("deprecation")
    public Mono<Authentication> getAuthentication(String token) {
        LOGGER.debug("getAuthentication for token: {}", token);
        try {
            // 解析令牌，获取声明体（Claims）
            Claims claims = Jwts.parser()
                .setSigningKey(key) // 设置用于验证签名的密钥
                .build()
                .parseClaimsJws(token) // 解析 JWT 令牌
                .getBody();
            // 从声明体中获取用户名
            String username = claims.getSubject();
            LOGGER.debug("Get username from token: {}", username);

            // 使用用户名加载用户信息
            return authenticationService.loadUserByUsername(username)
                .map(user -> {
                    LOGGER.debug("Get user from database: {}", user);
                    // 构建认证对象并返回
                    return new UsernamePasswordAuthenticationToken(user, token, user.getAuthorities());
                });
        } catch (JwtException | IllegalArgumentException e) {
            // 如果令牌无效，记录错误信息并返回 Mono.empty()
            LOGGER.error("JWT token is invalid: {}", e.getMessage());
            return Mono.just(null);
        }
    }

    /**
     * 验证 JWT 令牌是否有效。
     *
     * @param token JWT 令牌字符串
     * @return 如果令牌有效则返回 true，否则返回 false
     */
    @SuppressWarnings("deprecation")
    public boolean validateToken(String token) {
        LOGGER.debug("Validate JWT token: {}", token);
        try {
            // 解析令牌以验证其有效性
            Jwts.parser()
                .setSigningKey(key) // 设置用于验证签名的密钥
                .build()
                .parseClaimsJws(token); // 解析 JWT 令牌
            return true; // 如果解析成功则令牌有效
        } catch (JwtException | IllegalArgumentException e) {
            LOGGER.error("JWT token is invalid: {}", e.getMessage());
            return false; // 如果解析失败则令牌无效
        }
    }
}
