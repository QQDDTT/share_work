package com.nick.share_work.frame.authentication.model;

import java.util.Collection;
import org.springframework.security.core.userdetails.UserDetails;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * 用户对象
 * 
 * @author nick
 */
@JsonIgnoreProperties(ignoreUnknown = true) // 忽略未知属性的 JSON 反序列化
public class User implements UserDetails {

    private static final long serialVersionUID = 1L;

    private String id; // 用户 ID
    private String username; // 用户名
    private String password; // 密码
    private String email; // 电子邮件
    private Collection<Authority> authorities; // 权限集合

    /**
     * 默认构造函数
     */
    public User() {}

    /**
     * 构造函数
     * 
     * @param id 用户 ID
     * @param username 用户名
     * @param password 密码
     * @param email 电子邮件
     * @param authorities 权限集合
     */
    public User(String id, String username, String password, String email, Collection<Authority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.email = email;
        this.authorities = authorities;
    }

    /**
     * 获取用户的权限集合
     * 
     * @return 权限集合
     */
    @Override
    public Collection<Authority> getAuthorities() {
        return this.authorities;
    }

    /**
     * 设置用户的权限集合
     * 
     * @param authorities 权限集合
     */
    public void setAuthorities(Collection<Authority> authorities) {
        this.authorities = authorities;
    }

    /**
     * 获取用户 ID
     * 
     * @return 用户 ID
     */
    public String getId() {
        return id;
    }

    /**
     * 设置用户 ID
     * 
     * @param id 用户 ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * 获取用户名
     * 
     * @return 用户名
     */
    @Override
    public String getUsername() {
        return this.username;
    }

    /**
     * 设置用户名
     * 
     * @param username 用户名
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * 获取密码
     * 
     * @return 密码
     */
    @Override
    public String getPassword() {
        return this.password;
    }

    /**
     * 设置密码
     * 
     * @param password 密码
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * 获取电子邮件
     * 
     * @return 电子邮件
     */
    public String getEmail() {
        return email;
    }

    /**
     * 设置电子邮件
     * 
     * @param email 电子邮件
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * 获取账户信息
     * 
     * @return 账户信息
     */
    @Override
    public String toString() {
        return "User [id=" + id + ", username=" + username + ", password=" + password + ", email=" + email + ", authorities=" + authorities + "]";
    }
}
