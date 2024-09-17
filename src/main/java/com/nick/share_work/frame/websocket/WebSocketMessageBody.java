package com.nick.share_work.frame.websocket;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * WebSocketMessageBody 类用于封装 WebSocket 消息的内容，并提供将其转换为 JSON 字符串和从 JSON 字符串反序列化的方法。
 */
public class WebSocketMessageBody {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketMessageBody.class);
    private static final String SUCCESS = "success"; // 成功类型消息的标识
    private static final String ERROR = "error"; // 错误类型消息的标识

    private static final String UTF_8 = "utf-8"; // 字符编码
    
    private String type; // 消息类型
    private String key; // 消息的关键字
    private String message; // 消息内容
    private Map<String, String> value; // 附带的值

    // 无参构造函数，供 Jackson 反序列化使用
    public WebSocketMessageBody() {
    }

    // 带参数的构造函数
    private WebSocketMessageBody(String type, String key, String message, Map<String, String> value) {
        this.type = type;
        this.key = key;
        this.message = message;
        this.value = value;
    }

    // Getter 和 Setter 方法

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Map<String, String> getValue() {
        return value;
    }

    public void setValue(Map<String, String> value) {
        this.value = value;
    }
    
    public String toString() {
        return "WebSocketMessageBody [type=" + type + ", key=" + key + ", message=" + message + ", value=" + value + "]";
    }

    /**
     * 创建一个成功类型的 WebSocket 消息并转换为 JSON 字符串。
     * 
     * @param key 消息的关键字
     * @param message 消息内容
     * @param value 消息附带的值
     * @return JSON 字符串格式的消息
     */
    public static String success(String message, Map<String, String> value) {
        return createMessage(SUCCESS, UTF_8, message, value);
    }

    /**
     * 创建一个错误类型的 WebSocket 消息并转换为 JSON 字符串。
     * 
     * @param key 消息的关键字
     * @param message 消息内容
     * @param value 消息附带的值
     * @return JSON 字符串格式的消息
     */
    public static String error(String message, String reason) {
        return createMessage(ERROR, UTF_8, message, Map.of("reason", reason));
    }

    /**
     * 创建 WebSocket 消息并将其转换为 JSON 字符串。
     * 
     * @param type 消息类型
     * @param key 消息的关键字
     * @param message 消息内容
     * @param value 消息附带的值
     * @return JSON 字符串格式的消息
     */
    private static String createMessage(String type, String key, String message, Map<String, String> value) {
        LOGGER.debug("Creating WebSocketMessageBody with type {}, key {}, message {}", type, key, message);
        WebSocketMessageBody messageBody = new WebSocketMessageBody(type, key, message, value);
        return messageBody.toJson();
    }

    /**
     * 将 WebSocketMessageBody 对象转换为 JSON 字符串。
     * 
     * @return JSON 字符串格式的消息
     */
    private String toJson() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while converting WebSocketMessageBody to JSON: {}", e.getMessage(), e);
            return null;
        }
    }

    /**
     * 将 JSON 字符串反序列化为 WebSocketMessageBody 对象。
     * 
     * @param json JSON 字符串
     * @return WebSocketMessageBody 对象
     */
    public static WebSocketMessageBody fromJson(String json) {
        try {
            WebSocketMessageBody messageBody = new ObjectMapper().readValue(json, WebSocketMessageBody.class);
            LOGGER.debug("Converting JSON to WebSocketMessageBody: {}", messageBody);
            return messageBody;
        } catch (JsonMappingException e) {
            LOGGER.error("Error while mapping JSON to WebSocketMessageBody: {}", e.getMessage(), e);
        } catch (JsonProcessingException e) {
            LOGGER.error("Error while processing JSON to WebSocketMessageBody: {}", e.getMessage(), e);
        }
        return null;
    }
}
