# 新建项目
    
## 安装构建依赖/工具
* 安装好 **subversion** 客户端, 获取项目工程模板时会用到
* 安装好 **OpenJDK 8**
> 个人推荐使用 [*Zulu OpenJDK 8*](https://cn.azul.com/)
* 安装好 **Gradle 5.x** [*官网下载*](https://gradle.org/releases/)
* IDE推荐使用 **IntelliJ IDEA**, [*官网下载*](https://www.jetbrains.com/idea/download/)  
> Community社区版功能足以, 有钱的同学可以买 **Ultimate** 版
* 使用 windows10 的同学, 请安装 **WSL** 或 **WSL2**, 并在 WSL/WSL2 Linux子系统里安装好 OpenJDK 8 和 Gradle 5.x. 项目的编译,构建,打包等操作,请在 WSL/WSL2 环境下执行
* 使用 windows10 的同学, 推荐使用 [*Windows Terminal*](https://github.com/microsoft/Terminal) 这款微软出品的命令行工具

## 创建项目
现在假定我们创建一个叫 **Hello** 的应用.

* 从 [LoveInShenZhen/ProjectTemplates](https://github.com/LoveInShenZhen/ProjectTemplates) 获取工程模板.

```bash
svn export https://github.com/LoveInShenZhen/ProjectTemplates.git/trunk/vertx-web-simple Hello
```

## 项目目录结构
项目工程的目录结构如下, 具体的目录和配置文件的说明, 请阅读 _**ToDo**_ 文档.
```
Hello
├── build.gradle.kts
├── conf
│   ├── application.conf
│   ├── logback.xml
│   ├── route
│   └── route.websocket
├── gradle
│   └── wrapper
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties
├── gradlew
├── gradlew.bat
├── settings.gradle.kts
└── src
    └── main
        ├── kotlin
        │   ├── com
        │   │   └── api
        │   │       └── server
        │   │           ├── ApiServer.kt
        │   │           └── controllers
        │   │               └── sample
        │   │                   ├── SampleController.kt
        │   │                   └── reply
        │   │                       ├── HelloReply.kt
        │   │                       └── UserListReply.kt
        │   └── models
        │       └── sample
        │           └── User.kt
        └── resources

```

## 运行项目

```bash
# 进入到工程目录
cd Hello
# 构建项目, gradle自动下载依赖项
gradle build
# 运行项目
gradle run
```
![输出显示](../../img/vertx_web_simple_gradle_run.png)

## 在浏览器中查看启动页面
* 默认使用 9000 端口, 可以在 conf/application.conf 里进行配置
* 点击 [http://localhost:9000](http://localhost:9000) 在浏览器中查看启动页面

![启动页面](../../img/index_page.png)

## 查看自动生成的API接口测试页面
* 点击 **[api 列表](http://localhost:9000/api/builtin/doc/apiIndex)** 查看自动生成的APi测试页面

* API接口文档列表页面

![API接口文档列表页面](../../img/apiIndex_page.png)

* API接口测试页面, 填入参数, 点击 **测试** 按钮

![API接口测试页面](../../img/api_sample_hello.png)

## 查看自动生成的API接口文档
* 点击 **[api 文档的html格式](http://localhost:9000/api/builtin/doc/apiDocHtml)** 查看自动生成的API接口文档
* API接口文档页面

![API接口文档页面](../../img/api_doc_page.png)
