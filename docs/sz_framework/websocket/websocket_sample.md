# websocket 样例

## 样例准备实现如下的功能需求

* 后端为每个连接的 websocket 客户端分配一个唯一标识
* 连接成功后, 将唯一标识返回给客户端
* 后端提供一个 json api 用于查询当前连接的客户端列表(唯一标识列表, 连接时间)
* 后端提供一个 json api 用于向指定的客户端发送一段文本信息
* 后端提供一个 json api 用于向所有的客户端广播一段文本信息
* 客户端通过 websocket 向后端发送一个请求, 向指定的另外一个客户端发送一段文本信息
* 客户端通过 websocket 向后端发送一个请求, 向所有的客户端广播一段文本信息
* 后端提供一个 json api 用于强制断掉指定的客户端的连接
* 客户端和后端之间的消息是结构化的 JSON 字符串, 方便前后端进行处理

## 实现 websocket 功能的关键步骤
* 继承 **sz.scaffold.websocket.WebSocketHandler** 实现一个 websocket 处理器类  
* 在 **conf/route.websocket** 文件里配置 **websocket path** 和 **handler class**

## 关键代码演示

### 实现 WebSocketHandler

#### 关键步骤
* 重写方法: **override fun handle(webSocket: ServerWebSocket)**
* 在该方法里, 对新来的连接请求进行处理, 添加实现 webSocket 的几个handler *(回调函数)*
* 注: 框架会进行封装, 自动ping, 保持长连接的动作, 会由框架来完成. 客户端无须处理.

```kotlin
package com.api.server.websocket

import com.api.server.websocket.message.*
import io.vertx.core.eventbus.Message
import io.vertx.core.eventbus.MessageConsumer
import io.vertx.core.http.ServerWebSocket
import jodd.datetime.JDateTime
import sz.scaffold.Application
import sz.scaffold.eventbus.BeanMessageCodec
import sz.scaffold.ext.failed
import sz.scaffold.tools.json.toJsonPretty
import sz.scaffold.tools.json.toShortJson
import sz.scaffold.tools.logger.AnsiColor
import sz.scaffold.tools.logger.Logger
import sz.scaffold.websocket.WebSocketHandler
import sz.scaffold.websocket.queryParams
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap

/**
 * 注: 该类的实现必须是线程安全的.
 *     fun handle(webSocket: ServerWebSocket) 方法会被多个线程同时调用
 */
@Suppress("UNUSED_PARAMETER")
class SampleWsHandler : WebSocketHandler {

    /**
     * 当服务端收到一个新的 webSocket 连接请求时, 系统调用此方法
     */
    override fun handle(webSocket: ServerWebSocket) {

        // 获取 qurey 参数
        val queryParams = webSocket.queryParams()
        // 模拟检查token参数, 检查不通过则拒绝连接
        val token = queryParams.get("token")
        if (checkToken(token).failed()) {
            webSocket.reject(1000)
            return
        }

        // 分配一个唯一的 clientID
        val clientId = UUID.randomUUID().toString()

        // 注册 EventBus 消息处理函数
        val consumers = listOf(
            registBroadcastMsgConsumer(clientId, webSocket),
            registUnicastMsgConsumer(clientId, webSocket)
        )

        webSocket.closeHandler {
            // Set a close handler. This will be called when the WebSocket is closed.
            Logger.debug("WebSocket: [$clientId] is closed.")
            consumers.forEach { it.unregister() }
            clientMap.remove(clientId)
        }.exceptionHandler {
            // Set an exception handler on the read stream.
            Logger.warn("WebSocket: [$clientId] 有异常发生:\n$it")
        }.textMessageHandler {
            Logger.debug("WebSocket: [$clientId] 接收到客户端发过来的消息:\n$it", AnsiColor.YELLOW)
            onTextMessage(clientId, webSocket, it)
        }.pongHandler {
            Logger.debug("收到客户端 [$clientId] 的心跳 ping")
        }

        Logger.debug("收到 client 端的 web socket 请求, 分配 clientId: $clientId and accept it")
        webSocket.accept()

        clientMap[clientId] = WsClientInfo(
            clientId = clientId,
            webSocket = webSocket,
            connect_time = JDateTime()
        )

        // 将分配的 clientId 返回给客户端
        webSocket.writeFinalTextFrame(WsMessage.newMessage(ConnectedMsg(clientId)).toShortJson())
    }

    private fun checkToken(token: String): Boolean {
        // 模拟检查 token, 不为空字符串就当做检查通过
        // 检查通过返回true
        Logger.debug("token : $token")
        return token.isNotBlank()
    }

    private fun onEventBusBroadcastMsg(clientId: String, webSocket: ServerWebSocket, message: Message<WsMessage>) {
        webSocket.writeFinalTextFrame(message.body().toJsonPretty())
    }

    private fun onEventBusUnicastMsg(clientId: String, webSocket: ServerWebSocket, message: Message<WsMessage>) {
        webSocket.writeFinalTextFrame(message.body().toJsonPretty())
    }

    private fun registBroadcastMsgConsumer(clientId: String, webSocket: ServerWebSocket): MessageConsumer<WsMessage> {
        return Application.vertx.eventBus().consumer<WsMessage>(broadcastBusAddress) { msg ->
            onEventBusBroadcastMsg(clientId, webSocket, msg)
        }
    }

    private fun registUnicastMsgConsumer(clientId: String, webSocket: ServerWebSocket): MessageConsumer<WsMessage> {
        return Application.vertx.eventBus().consumer<WsMessage>(unicastBusAddress(clientId)) { msg ->
            onEventBusUnicastMsg(clientId, webSocket, msg)
        }
    }

    private fun onTextMessage(clientId: String, webSocket: ServerWebSocket, message: String) {
        val wsmsg = WsMessage.parseElseNull(message)
        if (wsmsg == null) {
            val errmsg = WsMessage.newMessage(TextMsg("Invalid message format."))
            webSocket.writeTextMessage(errmsg.toJsonPretty())
        } else {
            when (wsmsg.type_name) {
                BroadcastTextMsg::class.java.name -> onClientBroadcastTextMsg(clientId, webSocket, wsmsg)
                UnicastMsg::class.java.name -> onClientUnicastMsg(clientId, webSocket, wsmsg)
                TextMsg::class.java.name -> onClientTextMsg(clientId, webSocket, wsmsg)
                else -> onUnSupportedType(clientId, webSocket, wsmsg)
            }
        }
    }

    /**
     * 客户端通过 websocket 向后端发送一个请求, 向所有的客户端广播一段文本信息
     */
    private fun onClientBroadcastTextMsg(clientId: String, webSocket: ServerWebSocket, message: WsMessage) {
        broadcastMessage(message)
    }

    /**
     * 客户端通过 websocket 向后端发送一个请求, 向指定的另外一个客户端发送一段文本信息
     */
    private fun onClientUnicastMsg(clientId: String, webSocket: ServerWebSocket, message: WsMessage) {
        val msg = message.bodyObject() as UnicastMsg
        unicastMessage(msg.receiver, WsMessage.newMessage(UnicastMsg(msg.text, clientId, msg.receiver)))
    }

    private fun onClientTextMsg(clientId: String, webSocket: ServerWebSocket, message: WsMessage) {
        val msg = message.bodyObject() as TextMsg
        Logger.info("收到客户端[$clientId] 发来的消息: ${msg.text}")
    }

    /**
     * 处理不被支持的 WsMessage.type_name
     */
    private fun onUnSupportedType(clientId: String, webSocket: ServerWebSocket, message: WsMessage) {
        Logger.warn("UnSupported WsMessage.type_name: ${message.type_name}")
        val msg = WsMessage.newMessage(TextMsg("Unsupported message type: ${message.type_name}"))
        webSocket.writeTextMessage(msg.toJsonPretty())

    }

    companion object {

        val clientMap: ConcurrentMap<String, WsClientInfo> = ConcurrentHashMap()

        const val broadcastBusAddress = "websocket.SampleWsHandler.broadcast"

        init {
            Application.vertx.eventBus().registerDefaultCodec(WsMessage::class.java, BeanMessageCodec(WsMessage::class.java))
        }

        /**
         * 根据 clientId 得到该连接对应的 event bus 上的消息地址
         */
        fun unicastBusAddress(clientId: String): String {
            return "websocket.SampleWsHandler.unicast.$clientId"
        }

        /**
         * 通过 EventBus 广播一条消息, 最终会发送到所有的 websocket 客户端上
         */
        fun broadcastMessage(message: WsMessage) {
            Application.vertx.eventBus().publish(broadcastBusAddress, message)
        }

        /**
         * 通过 EventBus 单播一条消息, 最终会发送到由 clientId 指定的 websocket 客户端上
         */
        fun unicastMessage(clientId: String, message: WsMessage) {
            Application.vertx.eventBus().send(unicastBusAddress(clientId), message)
        }
    }
}
```

### 配置 websocket path 和 handler
* **conf/route.websocket** 添加一条配置, 如下所示:

```
# path              WebSocketHandler Class FullName
/ws/sample          com.api.server.websocket.SampleWsHandler
```

## 测试

### 测试页面(websocket 客户端)
* 新建一个 html 文件来进行测试, 代码如下所示:

```html
<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>WebSocketTest</title>
    <script type="text/javascript" src="http://www.w3school.com.cn/jquery/jquery-1.11.1.min.js"></script>
</head>

<body>
    <script>
        var socket;
        if (window.WebSocket) {
            socket = new WebSocket("ws://localhost:9000/ws/sample?token=fakeToken");
            // websocket收到消息
            socket.onmessage = function (event) {
                // 如果服务端是写的二进制数据，则此处的blob也是一个二进制对象，提取数据时需要Blob类和FileReader类配合使用
                var blob = event.data;
                console.log("websocket 收到数据:\n" + blob);
                var content = $("#content").html();
                $("#content").html(content + '<br>' + blob);
            };

            // websocket连接打开
            socket.onopen = function (event) {
                console.log("websocket 连接打开");
            };

            // websocket连接关闭
            socket.onclose = function (event) {
                console.log("websocket 连接关闭");
            };
        } else {
            alert("你的浏览器不支持websocket");
        }

        function send(message) {
            if (!window.WebSocket) {
                alert('浏览器不支持 WebSocket');
                return;
            }
            if (socket.readyState == WebSocket.OPEN) {
                socket.send(message);
                console.log("消息发送成功");
                
            } else {
                alert("websocket连接未打开，请检查网络设置");
            }
        }

        function clearInput() {
            document.getElementById('message').value = '';
        }
    </script>
    <form onsubmit="return false;">
        <!-- <input type="text" id="message" name="message" style="min-width: 480px;"> -->
        <textarea id="message" name="message" style="min-width: 640px; min-height: 320px;"></textarea>
        <input type="button" value="提交" onclick="send(this.form.message.value)">
        <button onclick="clearInput()">清除输入</button>
        <div id="content"></div>
    </form>
</body>

</html>
```

### 截图演示
![](../../img/hell_websocket_test_1.png)


## 完整代码
* 完整代码请移步: [https://github.com/LoveInShenZhen/ProjectTemplates/tree/master/samples/hello_websocket](https://github.com/LoveInShenZhen/ProjectTemplates/tree/master/samples/hello_websocket)

```bash
# 下载代码请执行如下命令
svn export https://github.com/LoveInShenZhen/ProjectTemplates.git/trunk/samples/hello_websocket hello_websocket
```

## 参考资料
* [Vert.x Event Bus](https://vertx.io/docs/vertx-core/kotlin/#event_bus)
* [Vert.x Event Bus api doc](https://vertx.io/docs/apidocs/io/vertx/core/eventbus/EventBus.html)
* [Vert.x websocket api doc](https://vertx.io/docs/apidocs/io/vertx/core/http/ServerWebSocket.html)