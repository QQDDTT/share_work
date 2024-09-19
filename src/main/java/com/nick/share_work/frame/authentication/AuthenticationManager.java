package com.nick.share_work.frame.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.nick.share_work.frame.jwt.JwtTokenProvider;

import reactor.core.publisher.Mono;

/**
 * 自定义认证管理器
 * 该类实现了 ReactiveAuthenticationManager 接口，用于处理用户认证。
 * 它依赖于 UserServiceImpl 和 PasswordEncoder 两个 Bean 来进行认证。
 * 
 * @author nick
 */
@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationManager.class);

    @Autowired
    private AuthenticationService authenticationService; // 用户服务，用于加载用户

    @Autowired
    private PasswordEncoder passwordEncoder; // 密码编码器，用于密码匹配

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // JWT 令牌提供者，用于生成 JWT 令牌

    /**
     * 进行用户认证。
     *
     * @param authentication 认证信息，包含用户名和密码
     * @return 包含认证结果的 Mono 对象
     * @throws AuthenticationException 认证异常
     */
    @Override
    public Mono<Authentication> authenticate(Authentication authentication) throws AuthenticationException {
        String username = authentication.getName(); // 获取用户名
        String password = (String) authentication.getCredentials(); // 获取密码

        LOGGER.debug("Authenticating user: {}", username); // 记录认证过程的日志

        // 从用户服务中加载用户
        return authenticationService.loadUserByUsername(username)
               .flatMap(user -> {
                    // 使用密码编码器验证密码是否匹配
                    if (passwordEncoder.matches(password, user.getPassword())) {
                        String token = jwtTokenProvider.generateToken(authentication); // 生成 JWT 令牌
                        LOGGER.debug("Generated JWT token for user: {}", username); // 记录生成 JWT 令牌的日志
                        // 创建认证成功的 Authentication 对象
                        Authentication auth = new UsernamePasswordAuthenticationToken(
                            user, // 用户对象
                            token, // JWT 令牌
                            user.getAuthorities() // 用户权限
                        );
                        return Mono.just(auth); // 返回认证成功的对象
                    } else {
                        LOGGER.debug("Invalid password for user: {}", username);
                        return Mono.error(new AuthenticationException("Invalid password") {});
                    }
               })
               .doOnError(error -> LOGGER.error("Error authenticating user: {} error : {}", username, error.getMessage())) // 记录认证失败的错误日志
               .onErrorResume(error -> Mono.error(new AuthenticationException("Authentication failed") {}));
    }
}
