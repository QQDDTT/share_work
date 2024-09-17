package com.nick.share_work.frame.authentication;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import com.nick.share_work.frame.jwt.JwtTokenProvider;

import reactor.core.publisher.Mono;

/**
 * 自定义认证成功处理器，用于处理用户成功登录后的行为。
 * 实现了ServerAuthenticationSuccessHandler接口，Spring Security会在用户成功认证后调用该处理器。
 * 
 * @author nick
 */
@Component
public class AuthenticationSuccessHandler implements ServerAuthenticationSuccessHandler {

    // 日志记录器，用于记录信息和调试信息
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationSuccessHandler.class);

    // JWT令牌提供者，用于生成和解析JWT令牌
    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    /**
     * 当用户成功认证时调用此方法。
     * 
     * @param webFilterExchange WebFilterExchange对象，包含请求和响应的上下文信息
     * @param authentication 经过认证的用户信息，包含用户的身份、权限等
     * @return Mono<Void> 返回一个表示操作完成的Mono对象
     */
    @Override
    public Mono<Void> onAuthenticationSuccess(WebFilterExchange webFilterExchange, Authentication authentication) {
        LOGGER.debug("onAuthenticationSuccess started");
        // 获取当前请求和响应的上下文信息
        ServerWebExchange exchange = webFilterExchange.getExchange();
        // 如果认证信息不为空，表示用户成功登录
        if (authentication != null) {
            LOGGER.debug("Authentication success: {}", authentication.getName());
            
            // 生成JWT令牌，用于后续的身份验证
            String token = jwtTokenProvider.generateToken(authentication);

            // 将生成的JWT令牌放入HTTP响应的Cookie中
            exchange.getResponse().getCookies().add("AUTH-TOKEN", ResponseCookie.from("AUTH-TOKEN", token)
                    .httpOnly(true)  // 设置Cookie为HttpOnly，防止客户端脚本访问
                    .path("/")        // 设置Cookie的路径为根路径
                    .build());

            // 存储用户信息到 Session
            return exchange.getSession()
                .doOnNext(webSession -> {
                    // 将用户信息存储到 Session 中
                    webSession.getAttributes().put("user", authentication.getPrincipal());
                })
                .then(Mono.fromRunnable(() -> {
                    // 设置HTTP响应状态码为302 Found，表示重定向到其他页面（通常是主页）
                    exchange.getResponse().setStatusCode(HttpStatus.FOUND);
                }));
        } else {
            // 如果认证信息为空，记录错误信息
            LOGGER.error("authentication is null");
        }
        // 返回一个空的Mono对象，表示操作完成
        return Mono.empty();
    }
}
