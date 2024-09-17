package com.nick.share_work.frame.authentication;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nick.share_work.frame.authentication.model.Authority;
import com.nick.share_work.frame.authentication.model.User;
import com.nick.share_work.frame.config.PropertiesReader;

import reactor.core.publisher.Mono;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map.Entry;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/** 
 * 用户服务实现类
 * 
 * @author nick
 */
@Component
public class AuthenticationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthenticationService.class);


    private final PropertiesReader propertiesReader;

    // Jackson 对象映射器，用于 JSON 处理
    private final ObjectMapper objectMapper = new ObjectMapper(); 

    // 线程安全的用户映射表
    private final Map<String, User> userMap = new ConcurrentHashMap<>(); 

    // 密码编码器
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder(); 

    /**
     * 构造函数，初始化用户服务。
     */
    public AuthenticationService(PropertiesReader propertiesReader) {
        this.propertiesReader = propertiesReader;
        loadUsersFromFile(); // 服务初始化时从文件加载用户数据
    }

    /**
     * 从文件加载用户数据到内存中的用户映射表。
     */
    private void loadUsersFromFile() {
        try {
            Path path = Paths.get(propertiesReader.getUserDataFilePath());
            // 检查文件是否存在
            if (Files.exists(path)) {
                // 从文件中读取用户数据并解析成 Map
                Map<String, User> loadedUsers = objectMapper.readValue(path.toFile(), new TypeReference<Map<String, User>>() {});
                
                LOGGER.debug("Loaded users from file: {}", loadedUsers);
                // 将读取到的用户数据放入用户映射表
                userMap.putAll(loadedUsers);
            } else {
                LOGGER.error("File not found: " + propertiesReader.getUserDataFilePath());
            }
        } catch (IOException e) {
            LOGGER.error("Failed to load users from file: " + propertiesReader.getUserDataFilePath(), e);
            e.printStackTrace();
        }
    }

    /**
     * 将内存中的用户数据保存到文件。
     */
    private synchronized void saveUsersToFile() throws IOException {
        try {
            File file = new File(propertiesReader.getUserDataFilePath());
            if (!file.exists()) {
                file.getParentFile().mkdirs(); // 创建目录
                file.createNewFile(); // 创建文件
                LOGGER.info("Created file: " + propertiesReader.getUserDataFilePath());
            }
            // 将内存中的用户数据写入到文件
            Path path = Paths.get(propertiesReader.getUserDataFilePath());
            objectMapper.writeValue(path.toFile(), userMap);
            LOGGER.info("Users saved to file: " + propertiesReader.getUserDataFilePath());
        } catch (IOException e) {
            LOGGER.error("Failed to save users to file: " + propertiesReader.getUserDataFilePath(), e);
            e.printStackTrace();
        }
    }

    /**
     * 根据用户名查找用户。
     * @param username 用户名
     * @return 用户对象
     */
    public Mono<User> loadUserByUsername(String username) {
        LOGGER.info("Loading user with username: " + username);
        for (Entry<String, User> entry : userMap.entrySet()) {
            if (entry.getValue().getUsername().equals(username)) { // 如果用户名匹配
                LOGGER.info("User loaded: " + username);
                return Mono.just(entry.getValue()); // 返回用户对象
            }
        }
        LOGGER.error("User not found: " + username);
        return Mono.just(new User()); // 如果用户名不存在，返回空用户对象
    }

    /**
     * 注册用户。
     * @param username 用户名
     * @param password 密码
     * @param email 邮箱
     * @return 注册成功的用户对象
     */
    public Mono<User> registerUser(String username, String password, String email) {
        LOGGER.info("Registering user with username: {}, password: {}, email: {}", username, password, email);
        for (Entry<String, User> entry : userMap.entrySet()) {
            if (entry.getValue().getUsername().equals(username)) { // 如果用户名已存在
                LOGGER.error("Username already exists: " + username);
                return Mono.empty(); // 注册失败
            }
        }
        // 生成随机 ID
        String id = UUID.randomUUID().toString(); // 生成随机 ID
        String encodedPassword = passwordEncoder.encode(password); // 加密密码
        User user = new User(id, username, encodedPassword, email, List.of(Authority.USER)); // 创建用户对象
        userMap.put(id, user);
        try {
            saveUsersToFile();
        } catch (IOException e) {
            LOGGER.error("Failed to save user to file: " + propertiesReader.getUserDataFilePath(), e);
            return Mono.empty(); // 注册失败
        } // 将用户数据保存到文件
        LOGGER.info("User registered successfully: username = {}" , username);
        return Mono.just(user); // 返回注册成功的用户对象
    }

    /**
     * 更新用户信息。
     * @param id 用户 ID
     * @param user 用户对象
     * @return 更新后的用户对象
     */
    public Mono<User> updateUser(String id, User user) {
        LOGGER.info("Updating user with id: {}", id);
        User oldUser = userMap.get(id);
        if (oldUser != null) { // 如果用户存在
            LOGGER.debug("User before update: user ={}", oldUser); // 打印用户信息
            userMap.put(id, user); // 更新用户数据
            try {
                saveUsersToFile(); // 将更新后的数据保存到文件
            } catch (IOException e) {
                LOGGER.error("Failed to save user to file: " + propertiesReader.getUserDataFilePath(), e);
                return Mono.empty(); // 更新失败
            } 
            LOGGER.info("User updated successfully");
            LOGGER.debug("User after update: user ={}", user); // 打印用户信息
            return Mono.just(user); // 返回更新后的用户对象
        }
        LOGGER.info("User update failed: " + id);
        return Mono.empty(); // 如果用户不存在，返回 null
    }

    /**
     * 删除用户。
     * @param id 用户 ID
     * @param user 用户对象
     * @return 删除成功返回 true，否则返回 false
     */
    public Mono<Boolean> deleteUser(String id, User user) {
        LOGGER.info("Deleting user with id: {}" , id);
        User oldUser = userMap.get(id);
        if (oldUser != null && oldUser.equals(user)) { // 如果用户存在且密码正确
            userMap.remove(id); // 删除用户数据
            try {
                saveUsersToFile(); // 将删除后的数据保存到文件
            } catch (IOException e) {
                LOGGER.error("Failed to save user to file: {}" , propertiesReader.getUserDataFilePath(), e);
                return Mono.just(false); // 删除失败
            }
            LOGGER.info("User deleted: {}" , id);
            return Mono.just(true); // 返回 true 表示删除成功
        }
        LOGGER.info("User deletion failed id : {}" , id);
        return Mono.just(false); // 如果用户名或密码错误，返回 false
    }
}
