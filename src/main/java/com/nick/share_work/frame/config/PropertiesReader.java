package com.nick.share_work.frame.config;

import java.util.Arrays;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;

@Component
public class PropertiesReader {

    @Autowired
    @Lazy
    private AntPathMatcher antPathMatcher;

    // 从配置文件中加载网页路径
    @Value("${index.page.url}")
    private String indexPageUrl;

    @Value("${login.page.url}")
    private String loginPath;

    @Value("${register.page.url}")
    private String registerPath;

    @Value("${update.page.url}")
    private String updatePath;

    @Value("${delete.page.url}")
    private String deletePath;

    @Value("${logout.page.url}")
    private String logoutPath;

    @Value("${login.success.page.url}")
    private String loginSuccessPath;

    @Value("${error.page.url}")
    private String errorPath;

    @Value("${favicon.url}")
    private String faviconUrl;

    @Value("${static.url}")
    private String staticUrl;

    @Value("${public.url}")
    private String publicUrl;

    @Value("${user.page.url}")
    private String userPageUrl;

    @Value("${admin.page.url}")
    private String adminUrl;

    @Value("${ajax.connoction.url}")
    private String ajaxUrl;

    /**
     * 获取白名单路径
     * @return 白名单路径
     */
    public String[] getWhitePaths() {
        return new String[]{"/", indexPageUrl, loginPath, registerPath, logoutPath, errorPath, faviconUrl, staticUrl, publicUrl};
    }

    /**
     * 获取白名单路径列表
     * @return 白名单路径列表
     */
    public List<String> getWhiteList() {
        return Arrays.asList(getWhitePaths());
    }

    /**
     * 获取一般用户路径
     * @return 一般用户路径
     */
    public String[] getUserPaths() {
        return new String[]{userPageUrl, updatePath, deletePath, ajaxUrl};
    }

    /**
     * 获取管理员路径
     * @return 管理员路径
     */
    public String[] getAdminPaths() {
        return new String[]{adminUrl, userPageUrl, updatePath, deletePath, ajaxUrl};
    }

    /**
     * 获取Ajax通信路径
     * @return Ajax通信路径
     */
    public String getAjaxPath(){
        return ajaxUrl;
    }

    /**
     * 判断是否是Ajax通信路径
     * @param path 路径
     * @return 是否是Ajax通信路径
     */
    public boolean isAjaxPath(String path){
        return path.startsWith(ajaxUrl);
    }

    /**
     * 判断是否是图标路径
     * @param path 路径
     * @return 是否是图标路径
     */
    public boolean isFaviconPath(String path){
        return faviconUrl.equals(path);
    }

    /**
     * 判断是否是静态资源路径
     * @param path 路径
     * @return 是否是静态资源路径
     */
    public boolean isStaticPath(String path){
        return antPathMatcher.match(staticUrl, path);
    }


    /**
     * 判断是否是登录页面路径
     * @param path 路径
     * @return 是否是登录页面路径
     */
    public boolean isLoginPath(String path){
        return loginPath.equals(path);
    }

    /**
     * 判断是否是注册页面路径
     * @param path 路径
     * @return 是否是注册页面路径
     */
    public boolean isRegisterPath(String path){
        return registerPath.equals(path);
    }

    /**
     * 判断是否是更新页面路径
     * @param path 路径
     * @return 是否是更新页面路径
     */
    public boolean isUpdatePath(String path){
        return updatePath.equals(path);
    }

    /**
     * 判断是否是删除页面路径
     * @param path 路径
     * @return 是否是删除页面路径
     */
    public boolean isDeletePath(String path){
        return deletePath.equals(path);
    }

    /**
     * 判断是否是登出页面路径
     * @param path 路径
     * @return 是否是登出页面路径
     */
    public boolean isLogoutPath(String path){
        return logoutPath.equals(path);
    }

    /**
     * 判断是否是错误页面路径
     * @param path 路径
     * @return 是否是错误页面路径
     */
    public boolean isErrorPath(String path){
        return errorPath.equals(path);
    }
    /**
     * 获取首页路径
     * @return 首页路径
     */
    public String getIndexPageUrl() {
        return indexPageUrl;
    }

    /**
     * 获取登录成功页面路径
     * @return 登录成功页面路径
     */
    public String getLoginSuccessPath() {
        return  loginSuccessPath;
    }

    /**
     * 获取错误页面路径
     * @return 错误页面路径
     */
    public String getErrorPath(){
        return errorPath;
    }


    // 从配置文件中加载websocket连接地址
    @Value("${echo.websocket.connection.url}")
    private String echoWebsocketConnectionUrl;

    @Value("${files.websocket.connection.url}")
    private String filesWebsocketConnectionUrl;

    /**
     * 获取echo websocket连接地址
     * @return
     */
    public String getEchoWebsocketConnectionUrl() {
        return echoWebsocketConnectionUrl;
    }

    /**
     * 获取files websocket连接地址
     * @return
     */
    public String getFilesWebsocketConnectionUrl() {
        return filesWebsocketConnectionUrl;
    }

    // 从配置文件中加载文件存储路径
    @Value("${files.base.path}")
    private String filesBasePath;

    @Value("${user.data.file.path}")
    private String userDataFilePath;

    /**
     * 获取文件存储路径
     * @return 文件存储路径
     */
    public String getFilesBasePath() {
        return filesBasePath;
    }

    /**
     * 获取用户数据文件路径
     * @return 用户数据文件路径
     */
    public String getUserDataFilePath() {
        return userDataFilePath;
    }

    // 从配置文件中加载favicon路径
    @Value("${favicon.path}")
    private String faviconPath;

    /**
     * 获取favicon路径
     * @return favicon路径
     */
    public String getFaviconPath() {
        return faviconPath;
    }
}
