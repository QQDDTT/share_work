package com.nick.share_work.frame.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.web.server.WebFilterExchange;
import org.springframework.stereotype.Component;
import org.springframework.util.ResourceUtils;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.yaml.snakeyaml.scanner.Scanner;

import com.nick.share_work.frame.authentication.AuthenticationConverter;
import com.nick.share_work.frame.authentication.AuthenticationManager;
import com.nick.share_work.frame.authentication.AuthenticationService;
import com.nick.share_work.frame.authentication.AuthenticationSuccessHandler;
import com.nick.share_work.frame.authentication.model.User;

import reactor.core.publisher.Mono;

import java.io.*;
import java.net.URI;
import java.util.Map;
import java.nio.charset.StandardCharsets;

/**
 * 处理成功请求和认证成功的处理器
 * 提供静态文件、HTML 页面服务，并处理认证成功后的重定向。
 * 
 * @author nick
 */
@Component
public class SuccessHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuccessHandler.class);

    @Autowired
    private PropertiesReader propertiesReader;

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthenticationSuccessHandler authenticationSuccessHandler;

    @Autowired
    private AuthenticationConverter authenticationConverter;

    /**
     * 处理 GET 请求
     * 
     * @param request 请求
     * @return 响应
     */
    public Mono<ServerResponse> get(ServerRequest request) {
        LOGGER.info("[GET] Request path: {}", request.path());
        if (request.path().equals("/")) {
            return getHTML(propertiesReader.getIndexPageUrl());
        } else if (propertiesReader.isLogoutPath(request.path())) {
            return logout(request); // 处理登出请求
        } else if (propertiesReader.isFaviconPath(request.path())) {
            return favicon(request); // 处理网站图标请求
        } else if (propertiesReader.isStaticPath(request.path())) {
            return getStatic(request); // 处理静态资源请求
        } else if (propertiesReader.isErrorPath(request.path())) {
            return errorPage(request.queryParam("message").get()); // 处理错误请求
        } else if (propertiesReader.isAjaxPath(request.path())) {
            return getAjax(request);
        } else {
            return getHTML(request.path());
        }
    }

    /**
     * 处理 POST 请求
     */
    public Mono<ServerResponse> post(ServerRequest request) {
        LOGGER.info("[POST] Request path: {}", request.path());
        if (propertiesReader.isLoginPath(request.path())) {
            return userLogin(request); // 处理登录请求
        } else if (propertiesReader.isRegisterPath(request.path())) {
            return userRegister(request); // 处理用户注册请求
        } else if (propertiesReader.isUpdatePath(request.path())) {
            return userUpdate(request); // 处理用户更新请求
        } else if (propertiesReader.isDeletePath(request.path())) {
            return userDelete(request); // 处理用户删除请求
        } else if (propertiesReader.isErrorPath(request.path())){
            return errorPage(request.queryParam("message").get()); // 处理错误请求
        } else {
            return getHTML(request.path()); // 处理 POST 请求，返回相应的 HTML 页面
        }
    }

    
    /**
     * 提供网站图标
     * 
     * @param request 请求
     * @return 响应
     */
    public Mono<ServerResponse> favicon(ServerRequest request) {
        Resource resource = new ClassPathResource(propertiesReader.getFaviconPath());
        return ServerResponse
                .ok()
                .contentType(MediaType.valueOf("image/x-icon"))
                .bodyValue(resource)
                .onErrorResume(e -> {
                    LOGGER.error("Error serving favicon: ", e);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * 提供静态资源
     * 
     * @param request 请求
     * @return 响应
     */
    public Mono<ServerResponse> getStatic(ServerRequest request) {
        String path = request.path();
        Resource resource = new ClassPathResource(path);
        LOGGER.info("[GET] Static resource request for path: {}", path);
        return ServerResponse
                .ok()
                .contentType(getMediaType(path))
                .bodyValue(resource)
                .onErrorResume(e -> {
                    LOGGER.error("Error serving static resource: ", e);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * 通过Ajax请求资源
     * 
     * @param request 请求
     * @return 响应
     */
    private Mono<ServerResponse> getAjax(ServerRequest request) {
        // 获取请求路径中的变量
        Map<String, String> pathVariables = request.pathVariables();
        pathVariables.put("null", null);
        // 在这里处理你的业务逻辑，例如根据路径变量获取数据
        String someData = "你的数据"; // 你可以用实际的数据替换这个

        // 构建一个JSON响应
        return ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(Mono.just(someData), String.class);
    }

    /**
     * 处理登出请求
     * 
     * @param request 请求
     * @return 响应
     */
    private Mono<ServerResponse> logout(ServerRequest request){
        return request.session().flatMap(session -> {
            session.invalidate();
            return getHTML(propertiesReader.getIndexPageUrl());
        });
    }

    /**
     * 提供 HTML 页面
     * 
     * @param path 路径
     * @return 响应
     */
    private Mono<ServerResponse> getHTML(String path) {
        Resource resource = new ClassPathResource("/templates" + path + ".html");
        LOGGER.info("[GET] HTML request for path: {}", path);
        return ServerResponse
                .ok()
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(resource)
                .onErrorResume(e -> {
                    LOGGER.error("Error serving HTML resource: ", e);
                    return ServerResponse.notFound().build();
                });
    }

    /**
     * 获取媒体类型
     * 
     * @param path 路径
     * @return 媒体类型
     */
    private MediaType getMediaType(String path) {
        if (path.endsWith(".js")) {
            return MediaType.valueOf("application/javascript");
        } else if (path.endsWith(".css")) {
            return MediaType.valueOf("text/css");
        } else if (path.endsWith(".html")) {
            return MediaType.TEXT_HTML;
        } else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
            return MediaType.IMAGE_JPEG;
        } else if (path.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        } else if (path.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        } else {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
    /**
     * 处理登录请求
     * 
     * @param request 请求
     * @return 响应
     */
    public Mono<ServerResponse> userLogin(ServerRequest request) {
        return authenticationConverter.convert(request.exchange())
                .flatMap(authentication -> {
                    LOGGER.info("[POST] userLogin2, authentication: {}", authentication);
                    return authenticationManager.authenticate(authentication);
                })
                .flatMap(auth -> {
                    LOGGER.info("[POST] userLogin2, authentication: {}", auth);
                    WebFilterExchange webFilterExchange = new WebFilterExchange(request.exchange(), ex -> Mono.empty());
                    return authenticationSuccessHandler.onAuthenticationSuccess(webFilterExchange, auth)
                            .then(ServerResponse.status(HttpStatus.FOUND)
                                    .location(URI.create(propertiesReader.getLoginSuccessPath()))
                                    .build());
                })
                .onErrorResume(e -> {
                    LOGGER.error("Error logging in user: ", e);
                    return errorPage("Error logging in user");
                });
    }
            

    /**
     * 处理用户注册请求
     * 
     * @param request 请求
     * @return 响应
     */
    public Mono<ServerResponse> userRegister(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.debug("[POST] userRegister, username: {}, password: {}, email: {}", username, password, email);
            return authenticationService.registerUser(username, password, email)
                    .flatMap(user -> request.session().flatMap(session -> {
                        return ServerResponse.status(HttpStatus.FOUND)
                                .location(URI.create(propertiesReader.getLoginSuccessPath()))
                                .build();
                    }))
                    .switchIfEmpty(errorPage("Error registering user"));
        });
    }

    /**
     * 处理用户更新请求
     * 
     * @param request 请求
     * @return 响应
     */
    public Mono<ServerResponse> userUpdate(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String id = formData.getFirst("id");
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.debug("[PUT] userUpdate, username: {}, password: {}, email: {}", username, password, email);
            User user = new User(id, username, password, email, null);
            LOGGER.debug("[PUT] userUpdate, user: {}", user);
            return authenticationService.updateUser(id, user)
                    .flatMap(newUser -> request.session().flatMap(session -> {
                        return ServerResponse.status(HttpStatus.FOUND)
                                .location(URI.create(propertiesReader.getLoginSuccessPath()))
                                .build();
                    }))
                    .switchIfEmpty(errorPage("Error updating user"));
        });
    }

    /**
     * 处理用户删除请求
     * 
     * @param request 请求
     * @return 响应
     */
    public Mono<ServerResponse> userDelete(ServerRequest request) {
        return request.formData().flatMap(formData -> {
            String id = formData.getFirst("id");
            String username = formData.getFirst("username");
            String password = formData.getFirst("password");
            String email = formData.getFirst("email");
            LOGGER.debug("[DELETE] userDelete, id: {}, username: {}, password: {}", id, username, password);
            User user = new User(id, username, password, email, null);
            return authenticationService.deleteUser(id, user)
                    .flatMap(deleted -> {
                        if (deleted) {
                            return ServerResponse.status(HttpStatus.FOUND)
                                    .location(URI.create(propertiesReader.getLoginSuccessPath()))
                                    .build();
                        } else {
                            LOGGER.error("Error deleting user");
                            return errorPage("Error deleting user");
                        }
                    });
        });
    }

    /**
     * 处理错误请求
     * 
     * @param message 错误信息
     * @return 响应
     */
    public Mono<ServerResponse> errorPage(String message) {
        LOGGER.info("[GET] Error page request for message: {}", message);
        Resource resource = new ClassPathResource("/templates" + propertiesReader.getErrorPath() + ".html");
    
        return Mono.fromCallable(() -> {
            try (InputStream inputStream = resource.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                StringBuilder content = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    content.append(line).append(System.lineSeparator());
                }
                String pageContent = content.toString().replace("${message}", message);
                return pageContent;
            } catch (IOException e) {
                LOGGER.error("Error reading error page: ", e);
                return "Error reading error page";
            }
        }).flatMap(content -> ServerResponse
                .status(HttpStatus.INTERNAL_SERVER_ERROR) // 500 Internal Server Error
                .contentType(MediaType.TEXT_HTML)
                .bodyValue(content))
        .onErrorResume(e -> {
            LOGGER.error("Error reading error page: ", e);
            return errorPage("Error reading error page");
        });
    }
}
