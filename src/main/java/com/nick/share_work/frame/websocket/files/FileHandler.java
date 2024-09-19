package com.nick.share_work.frame.websocket.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;

import reactor.core.publisher.Mono;

/**
 * 文件管理 WebSocket 处理器
 * 
 * @author nick
 */
@Component
public class FileHandler implements WebSocketHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FileHandler.class);

    @Autowired
    private FilesManagementServer fms;

    @SuppressWarnings("null")
    @Override
    public Mono<Void> handle(final WebSocketSession session) {
        LOGGER.info("[File Socket] Connection Established"); // 连接建立日志

        return session.send(
                session.receive()
                        .doOnNext(msg -> LOGGER.info("[Received Message]: {}", msg.getPayloadAsText())) // 记录接收到的消息
                        .map(msg -> {
                            String response = fms.getMsg(msg.getPayloadAsText()); // 获取处理后的响应消息
                            LOGGER.debug("[Response]: {}", response.length() > 20 ? response.substring(0, 10) + "..." : response); // 记录响应消息
                            return session.textMessage(response); // 发送响应消息
                        })
        );
    }
}
