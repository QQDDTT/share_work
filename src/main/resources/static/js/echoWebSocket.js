const CHAT_CONNECT = WS + HOST + "/echo_connect"; // 连接 WebSocket 服务器的地址
const SECURE_CHAT_CONNECT = WSS + HOST + "/echo_connect"; // 连接安全 WebSocket 服务器的地址

// 定义一个名为 ChatWS 的自执行函数
var ChatWS = (function() {
    var ws; // WebSocket 实例
    var loopSend; // 发送消息的定时器
    var recover; // 连接重试的定时器
    const TYPE = "echo"; //  WebSocket 通信的类型
    const CONNECT_STATUS = "创建链接成功, 开始发送消息"; // 连接成功的状态信息
    const RECOVER_STATUS = "链接断开, 重连..."; // 连接断开并尝试重连的状态信息
    const CLOSE_STATUS = "链接已断开"; // 连接关闭的状态信息
    const REFLESH_TIME = 1000; // 定时器刷新时间（毫秒）

    // ChatWS 构造函数
    function ChatWS(getSendMsgFunc, showDataFunc, statusFunc) {
        // 验证参数类型
        if (typeof getSendMsgFunc !== "function" || typeof showDataFunc !== "function") {
            console.error("Arguments must be function!");
            return;
        }
        this.getSendMsgFunc = getSendMsgFunc; // 用于获取要发送的消息的函数
        this.showDataFunc = showDataFunc; // 用于展示接收到的数据的函数
        // 状态函数，默认为打印状态信息到控制台
        this.statusFunc = typeof statusFunc === "function" ? statusFunc : function(status) {
            console.info("[STATUS] " + status);
        };
        this.connect(); // 初始化连接
    }

    // 连接到 WebSocket 服务器
    ChatWS.prototype.connect = function() {
        var self = this;
        // 如果已经有连接，则发出警告并返回
        if (self.ws) {
            console.warn('[警告] 已有连接');
            return;
        }

        self.ws = new WebSocket(CHAT_CONNECT); // 创建 WebSocket 实例
        self.statusFunc(CONNECT_STATUS); // 打印连接成功状态信息
        
        // 连接成功时的处理函数
        self.ws.onopen = function() {
            console.log('[连接已建立]');
            // 定时发送消息
            self.loopSend = setInterval(function() {
                // 检查 WebSocket 状态，确保可以发送消息
                if (self.ws.readyState === WebSocket.OPEN) {
                    var msg = self.getSendMsgFunc(); // 获取要发送的消息
                    if (msg) {
                        self.ws.send(getJson(TYPE, "", msg, {})); // 发送消息
                    } else {
                        console.warn('[警告] 未发送消息'); // 没有消息要发送的警告
                    }
                } else {
                    console.warn('[警告] WebSocket 状态不正确，不能发送消息');
                }
            }, REFLESH_TIME); // 定时发送消息
        };

        // 接收到消息时的处理函数
        self.ws.onmessage = function(event) {
            console.log('[服务器响应]', event.data);
            var data = JSON.parse(event.data); // 解析 JSON 数据
            self.showDataFunc(data.message); // 处理并展示接收到的数据
        };

        // 连接关闭时的处理函数
        self.ws.onclose = function(event) {
            self.statusFunc(RECOVER_STATUS); // 打印连接断开状态信息
            console.log('[连接关闭]', event.code, event.reason); // 打印连接关闭的状态码和原因
            reconnect(); // 尝试重连
        };

        // 连接错误时的处理函数
        self.ws.onerror = function(error) {
            console.error('[错误]', error.message || error); // 打印错误信息
            reconnect(); // 尝试重连
        };

        // 尝试重连的方法
        function reconnect() {
            clearInterval(self.loopSend); // 清除发送消息的定时器
            clearTimeout(self.recover); // 清除重连定时器
            self.recover = setTimeout(function() {
                self.connect(); // 重新建立连接
            }, REFLESH_TIME);
        }
    };

    // 断开连接的方法
    ChatWS.prototype.disconnect = function() {
        var self = this;
        // 如果没有连接，则发出警告并返回
        if (!self.ws) {
            console.warn('[警告] 没有连接可断开');
            return;
        }
        self.statusFunc(CLOSE_STATUS); // 打印连接关闭状态信息
        clearInterval(self.loopSend); // 清除发送消息的定时器
        clearTimeout(self.recover); // 清除重连定时器
        self.ws.onclose = function(event) {
            console.log('[连接已断开]', event); // 打印连接断开的信息
        };
        self.ws.onopen = null; // 清除连接打开的处理函数
        self.ws.onerror = null; // 清除连接错误的处理函数
        self.ws.close(); // 关闭 WebSocket 连接
        self.ws = null; // 清除 WebSocket 实例的引用
    };

    return ChatWS; // 返回 ChatWS 构造函数
})();
