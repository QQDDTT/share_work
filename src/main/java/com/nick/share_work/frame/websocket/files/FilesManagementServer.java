package com.nick.share_work.frame.websocket.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nick.share_work.frame.websocket.WebSocketMessageBody;


@Component
public class FilesManagementServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(FilesManagementServer.class);
    public static final String MSG_KEY = "message";
    public static final String PATH_KEY = "path";
    public static final String COND_KEY = "cond";
    public static final String VALUE_KEY = "value";
    public static final String LINE_KEY = "lineNum";

    private String message;
    private String path;
    private String cond;
    private String value;
    private String line;

    @Autowired
    private PathManage pathManage;

    @Autowired
    private FileManage fileManage;

    /**
     * 处理 WebSocket 消息并根据消息内容调用相应的文件管理方法
     * 
     * @param json JSON 格式的消息字符串
     * @return 处理结果的 JSON 字符串
     */
    public String getMsg(String json) {
        try {
            // 将 JSON 字符串反序列化为 WebSocketMessageBody 对象
            WebSocketMessageBody wsmb = WebSocketMessageBody.fromJson(json);
            this.message = wsmb.getMessage();
            this.path = wsmb.getValue().get(PATH_KEY);
            this.cond = wsmb.getValue().get(COND_KEY);
            this.value = wsmb.getValue().get(VALUE_KEY);
            this.line = wsmb.getValue().get(LINE_KEY);
            LOGGER.debug("message: {}, path: {}, cond: {}, value: {}, line: {}", message, path, cond, value, line);
            
            // 根据消息类型调用相应的处理方法
            switch (this.message) {
                case PathManage.EACH:
                    return pathManage.each();
                case PathManage.SEARCH:
                    return pathManage.search(cond);
                case PathManage.CREATE:
                    return pathManage.create(path);
                case PathManage.DELETE:
                    return pathManage.delete(path);
                case PathManage.END:
                    return pathManage.end();
                case FileManage.OPEN:
                    return fileManage.open(path);
                case FileManage.SAVE:
                    return fileManage.save();
                case FileManage.END:
                    return fileManage.end();
                case FileManage.READE_LINE:
                    return fileManage.readLine(line);
                case FileManage.WRITE_LINE:
                    return fileManage.writeLine(line, value);
                default:
                    return WebSocketMessageBody.error(message, "Unkonwn message type");
            }
        } catch (Exception e) {
            LOGGER.error("[ERROR] : {}", e);
            return "ERROR";
        }
    }
}
