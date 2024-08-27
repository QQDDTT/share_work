package com.nick.share_work.frame.websocket.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

/**
 * WebSocket 消息处理器，负责处理 WebSocket 连接和消息回显。
 * 
 * @author nick
 */
@Component
public class EchoHandler implements WebSocketHandler {
    
    // 使用 SLF4J 记录日志
    private static final Logger LOGGER = LoggerFactory.getLogger(EchoHandler.class);

    // 注入 EchoServer，用于处理接收到的消息
    @Autowired
    private EchoServer echoServer;

    /**
     * 处理 WebSocket 会话中的消息。
     *
     * @param session 当前的 WebSocket 会话
     * @return Mono<Void> 表示操作的完成
     */
    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        // 记录 WebSocket 连接建立的信息
        LOGGER.info("Echo WebSocket connection established");

        // 处理消息的发送和接收
        return session.send(
                session.receive()
                        .map(msg -> {
                            // 读取接收到的消息
                            String message = msg.getPayloadAsText();
                            LOGGER.debug("Received: {}", message);

                            // 通过 EchoServer 处理消息并生成响应
                            String response = echoServer.answer(message);
                            LOGGER.debug("Response: {}", response);

                            // 返回 WebSocket 文本消息
                            return session.textMessage(response);
                        })
        )
        // 连接关闭时的处理
        .doOnTerminate(() -> LOGGER.info("WebSocket connection closed"));
    }
}
