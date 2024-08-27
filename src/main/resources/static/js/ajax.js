const URL = AJAX_URL; // 这里应该替换成你的实际 URL
const METHOD = "GET";
const ERROR_MSG = "AJAX connection failed";

function getData(param, value) {
  // 创建一个 Promise 来处理异步请求
  return new Promise((resolve, reject) => {
    // 创建一个 XMLHttpRequest 对象
    var xhr = new XMLHttpRequest();

    // 配置请求的类型（GET/POST）、URL 和是否异步
    xhr.open(METHOD, URL + "?" + param + "=" + value, true);

    // 设置请求头（可选）
    xhr.setRequestHeader('Content-Type', 'application/json');

    // 注册事件处理函数，当请求状态发生变化时会调用
    xhr.onreadystatechange = function () {
      if (xhr.readyState === 4) {  // 请求已完成
        if (xhr.status === 200) {  // 响应状态为 200 OK
          try {
            // 解析 JSON 响应
            const data = JSON.parse(xhr.responseText);
            console.log(data);
            resolve(data); // 请求成功，返回数据
          } catch (error) {
            reject('Failed to parse response JSON'); // 解析 JSON 出错
          }
        } else {
          reject(ERROR_MSG); // 请求状态不是 200
        }
      }
    };

    // 发送请求（对于 POST 请求，可以在 send 方法中传入数据）
    xhr.send();
  });
}