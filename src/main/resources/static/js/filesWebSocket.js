const FILES_CONNECT = WS + HOST + "/files_connect"; // 连接 WebSocket 服务器的地址

// TYPES
const FILES_TYPE = "files"; // 文件传输的类型

// MESSAGES
const PATH_EACH = "path_each"; // 发送遍历路径的消息类型
const PATH_SEARCH = "path_search"; // 发送搜索路径的消息类型
const PATH_DELETE = "path_delete"; // 发送删除路径的消息类型
const PATH_CREATE = "path_create"; // 发送创建路径的消息类型    
const PATH_END = "path_end"; // 发送结束消息的消息类型
const FILE_OPEN = "file_open"; // 发送打开文件消息的消息类型
const FILE_SAVE = "file_save"; // 发送保存文件消息的消息类型
const FILE_END = "file_end"; // 发送结束消息的消息类型
const FILE_READ_LINE = "file_read_line"; // 发送读取文件的一行消息类型
const FILE_WRITE_LINE = "file_write_line"; // 发送写入文件的一行消息类型

/**
 * FilesWS 类用于处理与文件相关的 WebSocket 通信。
 */
var FilesWS = (function() {
    var ws = null; // WebSocket 实例
    var loop; // 定时器
    var recover; // 重新连接的定时器
    const TYPE = "files"; // WebSocket 通信的类型
    const CONNECT_STATUS = "创建链接成功, 开始发送消息"; // 连接成功的状态信息
    const RECOVER_STATUS = "链接断开, 重连..."; // 连接断开并尝试重连的状态信息
    const SEND_STATUS = "发送消息成功"; // 发送消息成功的状态信息
    const RECEIVE_STATUS = "接收消息成功"; // 接收消息成功的状态信息
    const ERROR_STATUS = "发送消息失败"; // 发送消息失败的状态信息
    const CLOSE_STATUS = "链接已断开"; // 连接关闭的状态信息
    const REFLESH_TIME = 1000; // 定时器刷新时间（毫秒）

    /**
     * FilesWS 构造函数
     * @param {Function} statusFunc - 用于显示状态信息的函数
     */
    function FilesWS(statusFunc) {
        // 状态函数，默认为打印状态信息到控制台
        this.statusFunc = typeof statusFunc === "function" ? statusFunc : function(status) {
            console.info("[STATUS] " + status);
        };
        this.connect(); // 初始化连接
    }

    /**
     * 连接到 WebSocket 服务器
     */
    FilesWS.prototype.connect = function() {
        var self = this;
        // 如果已经有连接，则发出警告并返回
        if (self.ws) {
            console.warn('[警告] 已有连接');
            return;
        }

        self.ws = new WebSocket(FILES_CONNECT); // 创建 WebSocket 实例

        // 连接打开时的处理函数
        self.ws.onopen = function() {
            self.loop = setInterval(function() {
                self.statusFunc(CONNECT_STATUS); // 打印连接成功状态信息
            }, REFLESH_TIME);
        }

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

        /**
         * 尝试重连的方法
         */
        function reconnect() {
            clearTimeout(self.recover); // 清除重连定时器
            self.recover = setTimeout(function() {
                self.connect(); // 重新建立连接
            }, REFLESH_TIME);
        }
    }

    /**
     * 断开 WebSocket 连接
     */
    FilesWS.prototype.disconnect = function() {
        var self = this;
        // 如果没有连接，则发出警告并返回
        if (!self.ws) {
            console.warn('[警告] 没有连接可断开');
            return;
        }
        self.statusFunc(CLOSE_STATUS); // 打印连接关闭状态信息
        clearInterval(self.loop); // 清除定时器
        clearTimeout(self.recover); // 清除重连定时器
        self.ws.onclose = function(event) {
            console.log('[连接已断开]', event); // 打印连接断开的信息
        };
        self.ws.onopen = null; // 清除连接打开的处理函数
        self.ws.onerror = null; // 清除连接错误的处理函数
        self.ws.close(); // 关闭 WebSocket 连接
        self.ws = null; // 清除 WebSocket 实例的引用
    };

    /**
     * 发送消息到 WebSocket 服务器
     * @param {string} message - 要发送的消息内容
     * @param {Object} value - 附带的值
     * @param {Function} showRecevieFunc - 用于展示接收到的数据的函数
     */
    FilesWS.prototype.send = function(message, value, showRecevieFunc) {
        if (typeof showRecevieFunc !== "function") {
            console.error("[错误] showRecevieFunc 必须是一个函数");
            return;
        }
        var self = this;
        // 如果没有连接，则发出警告并返回
        if (!self.ws) {
            console.warn('[警告] 没有连接，不能发送消息');
            return;
        }
        self.ws.send(getJson(TYPE, "", message, value)); // 发送消息
        self.statusFunc(SEND_STATUS); // 打印发送消息成功的状态信息
        self.ws.onmessage = function(event) {
            var data = JSON.parse(event.data); // 解析接收到的消息
            if (data.type === "error") {
                console.error('[错误]', data.message, 'REASON', data.value.reason); // 打印错误信息
                self.statusFunc(ERROR_STATUS);
            } else {
                self.statusFunc(RECEIVE_STATUS); // 打印接收消息成功的状态信息
                showRecevieFunc(data.value); // 调用显示接收消息的函数
            }
        }
    }
    return FilesWS; // 返回 FilesWS 构造函数
}());
