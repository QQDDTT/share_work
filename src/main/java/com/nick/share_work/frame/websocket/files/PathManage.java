package com.nick.share_work.frame.websocket.files;

import java.io.Closeable;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.nick.share_work.frame.config.PropertiesReader;
import com.nick.share_work.frame.websocket.WebSocketMessageBody;



/**
 * 路径管理类，用于管理文件路径
 * 
 * @author nick
 */
@Service
public class PathManage implements Closeable {
    private static final Logger LOGGER = LoggerFactory.getLogger(PathManage.class);

    private final PropertiesReader propertiesReader;

    // 定义常量，用于标识不同的文件操作
    public static final String EACH = "path_each";
    public static final String SEARCH = "path_search";
    public static final String CREATE = "path_create";
    public static final String DELETE = "path_delete";
    public static final String END = "path_end";

    private Map<String, String> model;
    private final Lock lock = new ReentrantLock(); // 锁，用于确保线程安全


    // 构造函数，初始化 model
    public PathManage(PropertiesReader propertiesReader) {
        this.propertiesReader = propertiesReader;
        this.model = new TreeMap<>(); // 使用 TreeMap 保证文件名的顺序
    }

    /**
     * 遍历 BASE_PATH 路径下的所有文件，并将其文件名和路径添加到模型中
     * 
     * @return 操作结果的 JSON 字符串
     */
    public String each() {
        lock.lock(); // 获取锁
        try {
            model.clear(); // 清空模型
            // 遍历路径下的所有文件
            try {
                String basePath = propertiesReader.getFilesBasePath();
                LOGGER.debug("PathManage each base path : {}", basePath);
                Files.walk(Paths.get(basePath))
                    .filter(Files::isRegularFile) // 仅处理文件，不处理目录
                    .forEach(p -> model.put(p.getFileName().toString(), p.toString())); // 将文件名和路径添加到模型
                LOGGER.debug("PathManage each model : {}", model);
                return WebSocketMessageBody.success(EACH, model); // 返回成功的 JSON 响应
            } catch (IOException e) {
                LOGGER.error("[EACH ERROR] : {}", e.getMessage());
                return WebSocketMessageBody.error(EACH, "Each error"); // 返回错误的 JSON 响应
            }
        } finally {
            lock.unlock(); // 释放锁
        }
    }

    /**
     * 根据条件在 BASE_PATH 下搜索文件
     * 
     * @param cond 文件名的匹配条件
     * @return 操作结果的 JSON 字符串
     */
    public String search(String cond) {
        lock.lock(); // 获取锁
        try {
            model.clear(); // 清空模型
            // 根据条件搜索文件
            try {
                Files.walk(Paths.get(propertiesReader.getFilesBasePath()))
                    .filter(p -> p.toString().matches(cond)) // 过滤匹配条件的文件
                    .forEach(p -> model.put(p.getFileName().toString(), p.toString())); // 将文件名和路径添加到模型
                return WebSocketMessageBody.success(SEARCH, model); // 返回成功的 JSON 响应
            } catch (IOException e) {
                LOGGER.error("[SEARCH ERROR] : {}", e.getMessage());
                return WebSocketMessageBody.error(SEARCH, "Search error"); // 返回错误的 JSON 响应
            }
        } finally {
            lock.unlock(); // 释放锁
        }
    }

    /**
     * 在 BASE_PATH 下创建文件或目录
     * 
     * @param path 要创建的文件或目录路径
     * @return 操作结果的 JSON 字符串
     */
    public String create(String path) {
        lock.lock(); // 获取锁以保证线程安全
        try {
            LOGGER.debug("PathManage create path : {}" + path);
            Path filePath = Paths.get(path);
            
            // 检查文件或目录是否已存在
            if (Files.exists(filePath)) {
                return WebSocketMessageBody.error(CREATE, "Create error: file or directory already exist"); // 文件或目录已存在，返回错误
            }
            
            // 根据路径的文件名判断是创建文件还是目录
            if (filePath.getFileName().toString().contains(".")) {
                // 如果路径包含点，则认为是文件，尝试创建文件
                try {
                    Files.createFile(filePath); // 创建文件
                    model.put(filePath.getFileName().toString(), filePath.toString()); // 将文件名和路径添加到模型
                    return WebSocketMessageBody.success(CREATE, model); // 返回成功的 JSON 响应
                } catch (IOException e) {
                    LOGGER.error("[CREATE FILE ERROR] : {}", e.getMessage());
                    return WebSocketMessageBody.error(CREATE, "Create file error"); // 返回文件创建错误的 JSON 响应
                }
            } else {
                // 如果路径不包含点，则认为是目录，尝试创建目录
                try {
                    Files.createDirectories(filePath); // 创建目录
                    model.put(filePath.getFileName().toString(), filePath.toString()); // 将目录名和路径添加到模型
                    return WebSocketMessageBody.success(CREATE, model); // 返回成功的 JSON 响应
                } catch (IOException e) {
                    LOGGER.error("[CREATE DIRECTORY ERROR] : {}", e.getMessage());
                    return WebSocketMessageBody.error(CREATE, "Create directory error"); // 返回目录创建错误的 JSON 响应
                }
            }
        } finally {
            lock.unlock(); // 释放锁
        }
    }

    /**
     * 删除指定路径的文件，并将其重命名为 .bk
     * 
     * @param path 要删除的文件路径
     * @return 操作结果的 JSON 字符串
     */
    public String delete(String path) {
        lock.lock(); // 获取锁
        try {
            Path filePath = Paths.get(path);
            Path backupPath = filePath.resolveSibling(filePath.getFileName() + ".bk");
            try {
                if (Files.exists(filePath) && !Files.exists(backupPath)) {
                    Files.move(filePath, backupPath); // 将文件重命名为 .bk
                    model.remove(filePath.getFileName().toString()); // 从模型中移除文件
                    return WebSocketMessageBody.success(DELETE, model); // 返回成功的 JSON 响应
                } else {
                    return WebSocketMessageBody.error(DELETE, "Delete error: file not exist or backup file already exist"); // 文件不存在或备份文件已存在，返回错误
                }
            } catch (IOException e) {
                LOGGER.error("[DELETE ERROR] : {}", e.getMessage());
                return WebSocketMessageBody.error(DELETE, "Delete error"); // 返回错误的 JSON 响应
            }
        } finally {
            lock.unlock(); // 释放锁
        }
    }

    /**
     * 结束路径管理操作并清理资源
     * 
     * @return 操作结果的 JSON 字符串
     */
    public String end() {
        lock.lock(); // 获取锁
        try {
            LOGGER.info("PathManage end");
            try {
                this.close(); // 清理资源
                return WebSocketMessageBody.success(END, null); // 返回成功的 JSON 响应
            } catch (IOException e) {
                LOGGER.error("[END ERROR] : {}", e.getMessage());
                return WebSocketMessageBody.error(END, "Close error"); // 返回错误的 JSON 响应
            }
        } finally {
            lock.unlock(); // 释放锁
        }
    }

    @Override
    public void close() throws IOException {
        this.model.clear(); // 清空模型
    }
}
