## http 路由配置 

### 路由配置文件
* **conf/route**

### 路由配置格式
* 举例说明:

```
# 测试接口
GET     /api/sample/hello       com.api.server.controllers.sample.SampleController.hello
GET     /api/some/api           com.api.server.controllers.sample.SampleController.someApi    {"arg1":"stringValue", "arg2":"true", "arg3"="100"}


```
*  如上所示, 一般路由定义分成3个部分, 第4部分为可选部分, 用于定义默认参数
* 第1部分, 指定该路由响应的 **[http method](https://developer.mozilla.org/en-US/docs/Web/HTTP/Methods)**, 框架支持: **GET**, **POST**, **HEAD** 三种, **要求大写**
* 第2部分, 指定该路由响应的 Http 请求的路径(Path)
* 第3部分, 指定该路由是由哪个控制器类的哪个方法进行处理. 举例说明如下:
> - **com.api.server.controllers.sample.SampleController.someApi** 控制器类 **SamSampleControllerple** 的 FullName
> - **hello** 是 **Sample** 这个控制器类中的一个方法, 用来处理http请求
* 第4部分, 指定当http请求的**Query String**部分不包含某参数的时候, 该参数所使用的默认值. 格式为一个 _**Map&lt;String, String&gt;**_ 的 _**单行 json 格式的字符串**_, 其中 key 为参数名称, value 为参数的默认值. 举例说明, 有路由配置如下:

```
GET  /api/some/api  com.api.server.controllers.sample.SampleController.someApi    {"arg1":"stringValueOfArg1", "arg2":"true", "arg3"="100"}

# 3个参数的值都不指定, 则3个参数都采用指定的默认值
http://localhost:9000/api/some/api
等价于:
http://localhost:9000/api/some/api?arg1=stringValueOfArg1&arg2=true&arg3=100

# 指定 arg1 和 arg3 的值, 但是不指定 arg2 的值, 则 arg2 会采用指定的默认值
http://localhost:9000/api/some/api?arg1=anotherValue&arg3=999
等价于:
http://localhost:9000/api/some/api?arg1=anotherValue&arg2=true&arg3=999
```

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

> 如果要用到其他不支持的类型, 请以 **String** 的方式传递, 然后在控制器方法里实现相应的类型转换操作


