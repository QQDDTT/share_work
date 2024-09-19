package com.nick.share_work.frame.jwt;

import java.net.URI;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.nick.share_work.frame.config.PropertiesReader;

import reactor.core.publisher.Mono;

/**
 * JwtAuthenticationFilter 类，用于处理 JWT 认证。
 * 该类实现了 WebFilter 接口，用于在请求处理链中进行 JWT 认证。
 * 该类从请求中提取 JWT token，并使用 JwtTokenProvider 进行认证，
 * 如果认证成功，则将认证信息写入上下文并继续过滤链，否则继续过滤链。
 * 如果没有找到 token，则继续过滤链。
 * 
 * @author nick
 */
@Component
public class JwtAuthenticationFilter implements WebFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    @Autowired
    private JwtTokenProvider jwtTokenProvider; // JWT 令牌提供者

    @Autowired
    private PropertiesReader propertiesReader; // 白名单属性

    @Autowired
    private AntPathMatcher pathMatcher; // 路径匹配器

    /**
     * 过滤方法处理每个请求，尝试提取和验证 JWT 令牌。
     * 如果令牌有效，它将设置认证上下文，并继续处理请求。
     *
     * @param exchange 当前的服务器交换
     * @param chain    web 过滤链
     * @return 当过滤链完成时完成的 Mono
     */
    @SuppressWarnings("null")
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        LOGGER.debug("JwtAuthenticationFilter filter");
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().toString();

        // 如果路径在白名单中，则直接返回，不进行认证
        if (isPathWhiteListed(path)){
            LOGGER.debug("Path is whitelisted: {}", path);
            return chain.filter(exchange); 
        }

        // 从请求中提取 JWT token
        String token = resolveToken(request);

        // 如果找到 token 并且验证通过
        if (token != null && jwtTokenProvider.validateToken(token)) {
            LOGGER.debug("Token found: {}", token);
            return jwtTokenProvider.getAuthentication(token)
                    .flatMap(authentication -> {
                        LOGGER.debug("Authentication successful: {}", authentication);
                        // 设置认证上下文，并继续处理请求
                        return chain.filter(exchange)
                                .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authentication));
                    })
                    .onErrorResume(e -> {
                        // 错误处理：记录错误日志，并返回 401 未授权状态
                        LOGGER.error("Authentication error : {}", e);
                        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
                        return exchange.getResponse().setComplete();
                    });
        } else {
            LOGGER.debug("Token validation failed");
            // 如果验证失败，设置状态码为 302，并重定向到首页
            exchange.getResponse().setStatusCode(HttpStatus.FOUND);
            exchange.getResponse().getHeaders().setLocation(URI.create("/"));
            return exchange.getResponse().setComplete();
        }
    }

    /**
     * 判断路径是否在白名单中。
     *
     * @param path 请求路径
     * @return 如果路径在白名单中，则返回 true，否则返回 false
     */
    private boolean isPathWhiteListed(String path) {
        return propertiesReader.getWhiteList().stream()
            .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }

    /**
     * 从请求中解析令牌
     * @param request 请求
     * @return token JWT token
     */
    @SuppressWarnings("null")
    private String resolveToken(ServerHttpRequest request){
        String path = request.getPath().toString();
        LOGGER.debug("resolveToken Path: {}", path);

        // 从请求中提取 JWT token
        String token = request.getCookies().getFirst("AUTH-TOKEN") != null
                ? request.getCookies().getFirst("AUTH-TOKEN").getValue()
                : null;
        LOGGER.debug("resolveToken token: {}", token);
        return token;
    }
}
