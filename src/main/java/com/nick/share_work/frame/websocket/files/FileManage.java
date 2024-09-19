package com.nick.share_work.frame.websocket.files;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nick.share_work.frame.websocket.WebSocketMessageBody;



/**
 * 文件管理器，用于管理文件内容的读写
 * 
 * @author nick
 */
@Service
public class FileManage implements Closeable{
    private static Logger LOGGER = LoggerFactory.getLogger(FileManage.class);

    // 常量定义，用于标识不同的文件操作
    public static final String OPEN = "file_open";
    public static final String SAVE = "file_save";
    public static final String END = "file_end";
    public static final String READE_LINE = "file_read_line";
    public static final String WRITE_LINE = "file_write_line";

    private String path; // 文件路径
    private Map<String, String> model; // 存储文件内容的 Map
    private Lock lock = new ReentrantLock(); // 锁，用于保证线程安全

    /**
     * 构造方法，初始化 Map
     */
    public FileManage() {
        this.model = new TreeMap<>();
    }

    /**
     * 打开指定路径的文件，并读取其内容到内存中
     * @param path 文件路径
     * @return 操作结果的 JSON 字符串
     */
    public String open(String path) {
        lock.lock();
        try {
            // 检查路径是否合法
            if (path == null || path.isEmpty()) {
                return WebSocketMessageBody.error(OPEN, "Invalid path provided");
            }

            // 记录文件打开操作
            LOGGER.info("[OPEN] Attempting to open file at path: {}", path);

            // 设置文件路径
            this.path = path;
            this.model.clear();
            String result;

            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(this.path), StandardCharsets.UTF_8))) {
                String line;
                int lineNumber = 1;
                while ((line = br.readLine()) != null) {
                    this.model.put(String.valueOf(lineNumber++), line);
                }
                LOGGER.debug("[OPEN] File content: {} ines", this.model.size());
                result = WebSocketMessageBody.success(OPEN, model);
            } catch (IOException e) {
                LOGGER.error("[OPEN ERROR] Failed to read file at path : {} ,error : {}", path, e.getMessage());
                result = WebSocketMessageBody.error(OPEN, "Failed to read file");
            }

            return result;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 将内存中的内容保存到文件中
     * 
     * @return 操作结果的 JSON 字符串
     */
    public String save() {
        lock.lock(); // 获取锁以保证线程安全
        try {
            LOGGER.info("[SAVE] Saving contents to file : {}", this.path);
            String result;
            
            // 尝试将内存中的内容写入到文件
            try (BufferedWriter bw = new BufferedWriter(
                    new OutputStreamWriter(new FileOutputStream(this.path), StandardCharsets.UTF_8))) {
                // 遍历内存中的所有行，将每一行写入到文件中
                for (String line : this.model.values()) {
                    bw.write(line); // 写入一行
                    bw.newLine(); // 写入换行符
                }
                result = WebSocketMessageBody.success(SAVE, null); // 成功保存内容，返回成功的 JSON 响应
            } catch (IOException e) {
                // 捕获并处理文件写入错误
                LOGGER.error("[SAVE ERROR] Failed to save content to file : {}", e.getMessage());
                result = WebSocketMessageBody.error(SAVE, null); // 返回保存错误的 JSON 响应
            }
            
            return result;
        } finally {
            lock.unlock(); // 释放锁
        }
    }


    /**
     * 结束文件操作，并关闭文件
     * @return 操作结果的 JSON 字符串
     */
    public String end(){
        try {
            this.close();
        } catch (IOException e) {
            return WebSocketMessageBody.error(END, "Close failed");
        }
        return WebSocketMessageBody.success(END, null);
    }

    /**
     * 关闭文件
     * @throws IOException
     */
    @Override
    public void close() throws IOException {
        lock.lock();
        try {
            this.path = null;
            this.model.clear();
            LOGGER.info("[CLOSE]");
        } finally {
            lock.unlock();
        }
    }

    /**
     * 读取指定行的内容
     * @param line 行号
     * @return 操作结果的 JSON 字符串
     */
    public String readLine(String line) {
        lock.lock();
        try {
            LOGGER.info("[READ_LINE] {}", line);
            String text = this.model.get(line);
            if (text == null) {
                return WebSocketMessageBody.error(READE_LINE, "Line not found");
            }
            return WebSocketMessageBody.success( READE_LINE, Map.of(line, text));
        } finally {
            lock.unlock();
        }
    }

    /**
     * 写入内容到指定行
     * @param line 行号
     * @param text 写入的内容
     * @return 操作结果的 JSON 字符串
     */
    public String writeLine(String line, String text) {
        lock.lock();
        try {
            LOGGER.info("[WRITE_LINE] {}", line);
            this.model.put(line, text);
            return WebSocketMessageBody.success(WRITE_LINE, null);
        } finally {
            lock.unlock();
        }
    }
}
