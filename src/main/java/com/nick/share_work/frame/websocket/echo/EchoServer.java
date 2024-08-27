package com.nick.share_work.frame.websocket.echo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.nick.share_work.frame.websocket.WebSocketMessageBody;

/**
 * 处理 WebSocket 消息的服务类，用于回显消息。
 * 
 * @author nick
 */
@Component
public class EchoServer {
    
    // 使用 SLF4J 记录日志
    private static final Logger LOGGER = LoggerFactory.getLogger(EchoServer.class);

    /**
     * 处理收到的消息并返回响应。
     *
     * @param json 收到的消息的 JSON 字符串
     * @return 回显的消息
     */
    public String answer(String json) {
        LOGGER.debug("[RECEIVED]: " + json);
        // 将 JSON 字符串转换为 WebSocketMessageBody 对象
        WebSocketMessageBody webSocketMessageBody = WebSocketMessageBody.fromJson(json);
        // 从消息体中提取消息内容
        String message = "RESPONSE : " + webSocketMessageBody.getMessage();
        LOGGER.debug("[MESSAGE]: " + message);

        // 返回成功的响应消息
        return WebSocketMessageBody.success(message, null);
    }
}
