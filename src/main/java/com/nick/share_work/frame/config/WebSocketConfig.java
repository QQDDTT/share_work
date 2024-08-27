package com.nick.share_work.frame.config;

import java.io.FileInputStream;
import java.security.KeyStore;
import java.util.HashMap;
import java.util.Map;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.config.EnableWebFlux;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import com.nick.share_work.frame.websocket.echo.EchoHandler;
import com.nick.share_work.frame.websocket.files.FileHandler;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;

/**
 * WebSocket 配置类。
 * 
 * @author nick
 */
@Configuration
@EnableWebFlux
public class WebSocketConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketConfig.class);

    @Autowired
    private PropertiesReader propertiesReader;

    // 配置 WebSocket 处理器
    @Autowired
    private EchoHandler echoHandler;

    // 配置文件管理 WebSocket 处理器
    @Autowired
    private FileHandler fileHandler;

    /**
     * 配置 WebSocket 映射。
     * 
     * @param echoHandler EchoHandler 对象，用于处理 WebSocket 连接。
     * @param fileHandler FileHandler 对象，用于处理文件管理 WebSocket 连接。
     * @return 配置好的 HandlerMapping 对象。
     */
    @Bean
    public HandlerMapping webSocketMapping() {
        LOGGER.debug("Configuring WebSocket mapping");
        final Map<String, WebSocketHandler> map = new HashMap<>();
        map.put(propertiesReader.getEchoWebsocketConnectionUrl(), echoHandler); // 配置 Echo WebSocket 处理器
        map.put(propertiesReader.getFilesWebsocketConnectionUrl(), fileHandler); // 配置文件管理 WebSocket 处理器
        final SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setOrder(Ordered.HIGHEST_PRECEDENCE); // 设置映射优先级
        mapping.setUrlMap(map); // 设置 URL 到处理器的映射
        LOGGER.debug("WebSocket mapping configured");
        return mapping;
    }

    /**
     * 配置 WebSocket 处理器适配器。
     * 
     * @return WebSocketHandlerAdapter 对象。
     */
    @Bean
    public WebSocketHandlerAdapter handlerAdapter() {
        LOGGER.debug("Configuring WebSocket handler adapter");
        return new WebSocketHandlerAdapter();
    }


    /**
     * 获取 SSL 上下文
     */
    public SslContext getSslContext() {
        try {
            LOGGER.debug("Configuring SSL/TLS");
            // 加载密钥库
            KeyStore keyStore = KeyStore.getInstance(propertiesReader.getKeyStoreType());
            try (FileInputStream is = new FileInputStream(propertiesReader.getKeyStorepath())) {
                keyStore.load(is, propertiesReader.getKeyStorePasswordCharArray());
            }

            // 初始化 KeyManagerFactory
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(keyStore, propertiesReader.getKeyStorePasswordCharArray());

            // 初始化 TrustManagerFactory
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // 配置 SSL 上下文
            SslContextBuilder sslContextBuilder = SslContextBuilder.forServer(keyManagerFactory)
                    .trustManager(trustManagerFactory);

            SslContext sslContext = sslContextBuilder.build();
            LOGGER.debug("SSL/TLS configuration successful");
            return sslContext;
        } catch (Exception e) {
            LOGGER.error("Failed to configure SSL/TLS", e);
            return null;
        }
    }
}
