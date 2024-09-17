package com.nick.share_work.frame.authentication;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.reactive.result.view.ViewResolver;
import org.springframework.web.server.ServerWebExchange;

import com.nick.share_work.frame.config.PropertiesReader;

import reactor.core.publisher.Mono;

/**
 * 自定义的 ServerAccessDeniedHandler 实现，用于处理访问被拒绝时的响应。
 * 当用户访问被拒绝时，返回 403 状态码，并进行重定向到指定的错误页面。
 * 
 * @author nick
 */
@Component
public class AuthenticationAccessDeniedHandler implements ServerAccessDeniedHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationAccessDeniedHandler.class);

    @Autowired
    private PropertiesReader propertiesReader;

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, AccessDeniedException denied) {
        // 记录访问被拒绝的警告日志
        LOGGER.warn("Access denied for user: {}", denied.getMessage());

        // 准备错误页面所需的模型数据
        String encodedMessage = URLEncoder.encode(denied.getMessage(), StandardCharsets.UTF_8);
        String redirectPath = propertiesReader.getErrorPath() + "?message=" + encodedMessage;

        // 从配置中获取重定向的 URI
        URI redirectUri = URI.create(redirectPath);
        LOGGER.debug("Redirecting to: {}", redirectUri);

        // 使用 create 方法构建 ServerResponse
        return ServerResponse.status(HttpStatus.TEMPORARY_REDIRECT)
            .header(HttpHeaders.LOCATION, redirectUri.toString())
            .build()
            .flatMap(response -> response.writeTo(exchange, new HandlerStrategiesResponseContext()));
    }

    // 内部类，用于提供 ServerResponse 所需的上下文
    private static class HandlerStrategiesResponseContext implements ServerResponse.Context {

        @SuppressWarnings("null")
        @Override
        public List<HttpMessageWriter<?>> messageWriters() {
            // 提供消息写入器
            return HandlerStrategies.withDefaults().messageWriters();
        }

        @SuppressWarnings("null")
        @Override
        public List<ViewResolver> viewResolvers() {
            // 提供视图解析器
            return HandlerStrategies.withDefaults().viewResolvers();
        }
    }
}
