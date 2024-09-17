package com.nick.share_work.frame.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.reactive.function.server.RequestPredicate;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import com.nick.share_work.frame.authentication.AuthenticationAccessDeniedHandler;
import com.nick.share_work.frame.authentication.model.Authority;
import com.nick.share_work.frame.jwt.JwtAuthenticationFilter;
import com.nick.share_work.frame.websocket.files.FileManage;
import com.nick.share_work.frame.websocket.files.PathManage;



/**
 * 安全配置类，用于配置 Spring Security 相关的设置。
 * 
 * @author nick
 */
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private PropertiesReader propertiesReader;

    /**
     * 配置安全过滤链。
     * 
     * @param http ServerHttpSecurity 对象，用于配置安全设置。
     * @param authenticationEntryPoint 自定义认证入口点。
     * @param jwtAuthenticationFilter JWT 认证过滤器。
     * @return 配置好的 SecurityWebFilterChain 对象。
     */
    @SuppressWarnings("removal")
    @Bean
    public SecurityWebFilterChain securityFilterChain(ServerHttpSecurity http, 
                                                    AuthenticationAccessDeniedHandler accessDeniedHandler,
                                                    JwtAuthenticationFilter jwtAuthenticationFilter) {
        LOGGER.debug("securityFilterChain started");
        return http.authorizeExchange(exchange -> {
                        exchange.pathMatchers(propertiesReader.getWhitePaths()).permitAll() // 允许所有人访问
                                .pathMatchers(propertiesReader.getUserPaths()).hasAnyAuthority(Authority.USER.getAuthority()) // 允许用户访问
                                .pathMatchers(propertiesReader.getAdminPaths()).hasAnyAuthority(Authority.ADMIN.getAuthority()) // 允许管理员访问
                                .anyExchange().authenticated(); // 其他所有请求都需要认证
                    })
                .csrf().disable() // 禁用 CSRF 保护
                .exceptionHandling(exceptionHandling -> {
                        exceptionHandling
                            .accessDeniedHandler(accessDeniedHandler) // 使用自定义的认证失败处理器
                            ; 
                    })
                .addFilterAt(jwtAuthenticationFilter, SecurityWebFiltersOrder.AUTHENTICATION) // 添加 JWT 认证过滤器
                .build();
    }
    
    /**
     * 配置自定义认证失败处理器。
     * 
     * @return CustomAuthenticationAccessDeniedHandler 对象。
     */
    @Bean
    public AuthenticationAccessDeniedHandler accessDeniedHandler() {
        LOGGER.debug("accessDeniedHandler started");
        return new AuthenticationAccessDeniedHandler();
    }

    /**
     * 配置路由功能。
     * 
     * @param handler SuccessHandler 对象，用于处理不同的请求。
     * @return 配置好的 RouterFunction 对象。
     */
    @Bean
    public RouterFunction<ServerResponse> routes(SuccessHandler successHandler) {
        LOGGER.debug("routes started");
        return RouterFunctions
            .route(isHttp()
                    .and(RequestPredicates.method(HttpMethod.GET))
                    .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    successHandler::get) // 处理 GET 请求
            .andRoute(isHttp()
                    .and(RequestPredicates.method(HttpMethod.POST))
                    .and(RequestPredicates.accept(MediaType.TEXT_HTML)),
                    successHandler::post); // 处理 POST 请求
    }

    /**
     * 配置密码编码器。
     * 
     * @return PasswordEncoder 对象。
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        LOGGER.debug("passwordEncoder started");
        return new BCryptPasswordEncoder();
    }

    /**
     * 配置 AntPathMatcher 对象。
     * 
     * @return AntPathMatcher 对象。
     */
    @Bean
    public AntPathMatcher antPathMatcher() {
        LOGGER.debug("antPathMatcher started");
        return new AntPathMatcher();
    }

    /**
     * 配置路径管理器。
     * 
     * @return PathManage 对象。
     */
    @Bean
    public PathManage pathManage(PropertiesReader propertiesReader) {
        LOGGER.debug("pathManage started");
        return new PathManage(propertiesReader);
    }

    /**
     * 配置文件管理器。
     * 
     * @return FileManage 对象。
     */
    @Bean
    public FileManage fileManage() {
        LOGGER.debug("fileManage started");
        return new FileManage();
    }

    /**
     * 检查请求是否为 HTTP 请求。
     * 
     * @return RequestPredicate 对象。
     */
    private RequestPredicate isHttp() {
        return request -> "http".equalsIgnoreCase(request.uri().getScheme()) || "https".equalsIgnoreCase(request.uri().getScheme());
    }
}
