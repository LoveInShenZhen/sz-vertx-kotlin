## 配置文件 

### 配置文件路径
* conf/application.conf

### 配置文件语法
* [HOCON（人性化配置对象表示法，Human-Optimized Config Object Notation）](https://github.com/ustc-zzzz/HOCON-CN-Translation/blob/master/HOCON.md)

### 配置文件读取所使用的java库
* [Typesafe Config](https://lightbend.github.io/config/) 
* [Typesafe lightbend/config github 站点](https://github.com/lightbend/config)
* [Java doc](https://lightbend.github.io/config/latest/api/)

### 在sz框架里面读取配置
* 通过一个全局的 **sz.scaffold.Application** object 提供的 **config** 属性来读取 conf/application.conf 配置文件里对应的配置项
* [config对象javadoc](https://lightbend.github.io/config/latest/api/com/typesafe/config/Config.html)

```kotlin
package sz.scaffold

object Application {
    val config: Config
    ...
}
```

* sample code

> conf/application.conf 配置如下:

```
app {
  testSite = true

  httpServer {
    port = 9000
    host = "0.0.0.0"
  }
}
```
> 读取对应的配置, 注意配置的 **path** 体现了配置对应的层级关系

```kotlin
val isTestSite = Application.config.getBoolean("app.testSite")
val port = Application.config.getInt("app.httpServer.port")
val host = Application.config.getString("app.httpServer.host")

```

