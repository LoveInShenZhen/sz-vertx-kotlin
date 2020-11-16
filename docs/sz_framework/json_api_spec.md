## sz-json api 规范 

* SZ 是一套基于 **前后端分离** 的思想, 专门用于 后端 (业务,应用服务器)的 快速开发框架.
* 前端静态HTML页面是通过 **ajax** 的方式, 调用后端提供的 **http api** 接口
* sz 框架定义了一套 **http json api** 的规范

### Request-Response 形式
* **Http Request**, 支持 **GET**, **POST**, **HEAD** 这三种方式

    > - 因为 **阿里云的负载均衡服务** 是通过 **HEAD** 方法对后端进行健康检查的, 所以框架对 HEAD 予以支持
    > - 通常API接口, 采用 **GET** 或者 **POST** 即可

* Response 返回一个Json文本
* Response 的 **Content-Type** : **application/json; charset=utf-8**
* Response 的 **transfer-encoding** : **chunked**

### Response 的 Json 格式
* 所有的Response都包括如下的2个基础字段:
```json
{
    "ret": 0,
    "errmsg": "OK",
}
```

    > - **ret** : 0 表示成功, 其他值表示错误码, 由开发人员自己定义. 
    > - **errmsg** : 当ret=0时, 返回OK, 非0时, 返回错误描述信息. 例如: token超时; 违背某某业务规则; 某某参数为空 等等
    > - 这2个基础字段, 是从 **ReplyBase** 类继承而来的. 
    > - web前端程序, 应当首先对调用返回的 **ret** 值进行判断, 如果不为 0 则需要进行相应的错误处理


### 字符编码要求 utf-8
* Request 和 Response 的字符编码要求是: charset=utf-8
* **Content-Type** : **application/json; charset=utf-8**

### api参数传递方式
* 每个api请求(http request), 会根据route配置, 指定对应的控制器类(实例)的某个方法去处理
* 该控制器方法上可以定义若干参数, 这些参数的值, 要求通过 **Query String** 方式进行传递, 按照参数名称传值, 与顺序无关
* 控制器方法的参数类型有要求, 只支持如下几种:   

|       类型名称       |
| -------------------- |
| kotlin.String        |
| kotlin.Int           |
| kotlin.Short         |
| kotlin.Byte          |
| kotlin.Long          |
| kotlin.Float         |
| kotlin.Double        |
| kotlin.Boolean       |
| java.math.BigDecimal |

> 如果要用到其他不支持的类型, 请以 **String** 的方式传递, 在控制器方法里实现相应的转换操作