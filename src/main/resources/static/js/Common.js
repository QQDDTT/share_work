const HTTP = "http://";
const HTTPS = "https://";
const WS = "ws://";
const WSS = "wss://";

const HOST = location.host;
/**
 * 定义一些常用的 URL
 */
const USER_LOGIN_URL  = HTTP + HOST + "/login";
const USER_REGISTER_URL  = HTTP + HOST + "/register";
const USER_UPDATE_URL  = HTTP + HOST + "/update";
const USER_DELETE_URL  = HTTP + HOST + "/delete";
const USER_LOGOUT_URL  = HTTP + HOST + "/logout";
const HOME_URL  = HTTP + HOST + "/home";
const AJAX_URL = HTTP + HOST + "/ajax";
const FILES_URL  = HTTP + HOST + "/admin/files/files";
const EDITOR_URL  = HTTP + HOST + "/admin/files/editor";
const ECHO_URL  = HTTP + HOST + "/user/chat/echo";
const DEMO_URL  = HTTP + HOST + "/public/demo";

/**
 * 根据提供的 URL 执行页面跳转
 * @param {string} url - 要跳转的目标 URL
 */
function action(url){
    window.location.href = url;
}

/**
 * 生成 JSON 字符串
 * @param {string} type - 消息类型
 * @param {string} key - 消息键
 * @param {string} message - 消息内容
 * @param {object|Map} value - 附加值
 * @returns {string} - JSON 字符串
 */
function getJson(type, key, message, value){
    var obj = {type: type, key: key, message: message};
    if (typeof value === "object" && !Array.isArray(value)) {
        obj.value = value;
    } else if (value instanceof Map) {
        obj.value = Object.fromEntries(value.entries());
    } else {
        obj.value = {};
    }
    return JSON.stringify(obj);
}
