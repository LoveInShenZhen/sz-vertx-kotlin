# sz-vertx-kotlin 后端快速开发框架

详情请查看[网站文档](http://loveinshenzhen.github.io/#/)

下文将使用 **sz框架** 简称 

## 什么是 sz框架

* **sz框架** 是一套在**前后端分离**的开发模式下, 专注于**后端http api接口**的快速开发框架 

# 设计理念

* **sz** 不是像 **Spring 系列框架** 那种大而全的框架, 目标范围仅限于: 快速开发一个 http api server

* **sz** 作为一套开发**框架**, 希望可以对使用者做尽量少的限制. 目前, 它对使用者的限制有:

  * 开发语言采用 **Kotlin** (原因是我个人认为, Kotlin 比 Java 更具生产力和开发效率. 如果你不能认同和接收这点, 说明**sz框架**不符合你的口味)
  * 核心底层强依赖于 [**Vert.x-Web**](https://vertx.io/docs/vertx-web/kotlin/). 使用 Vert.x-Web 来完成 http 协议的相关处理. 所以, **sz框架** 是不需要J2EE相关依赖的. sz框架**是通过对 Vert.x-Web 进行包装, 来实现和简化 **快速构建一个http api server** 这一任务目标的.
  * 框架的配置文件采用了 **HOCON** 格式, 配置文件读取的库采用了**[Typesafe Config](https://github.com/lightbend/config)** (这也是 [Play Framework](https://www.playframework.com/) 所采用的配置文件处理的库)
  * Log 采用了 **SL4j** + **Logback** 的组合.
  * 抛弃了 **REST** 风格, 自定义了一套 [**http json api** 的语义规范](http://loveinshenzhen.github.io/#/sz_framework/json_api_spec). GET, POST, PUT,DELETE 这些http method 不再代表特殊的语义. 
  * http api返回的json, 是通过定义不同的 Reply 类(继承自ReplyBase)来实现的. 通过**强类型的类结构**, 来保障返回的Json结构的正确性. 同时,  **sz-api-doc** 也会通过反射的方式, 根据对应的 Reply 类的结构, 生成对应的, 描述返**回结果 json 的树状结构描述**的文档.
  * http api 接口参数的传递方式, 只支持 **QueryString**, **Post Form**, **Post Json**, 不采用 path 路径上通过正则表达式来定义参数的用法. 目的是为了简化, 降低任务出错的可能性. (正则表达式太不直观了)
  * 通过 route 文件定义 api 路径到控制器方法的映射, [参见](http://loveinshenzhen.github.io/#/sz_framework/http_route). 没有采用通过注解进行路由映射定义的方式. 因为采用注解的方式, 会导致路由定义分散到很多个不同的代码文件里, 我认为这样会提升代码的维护难度.

* 因为底层采用的是 Vert.x, 所以**sz框架**天然就能够利用 Vert.x 带来的各种能力. 具体请查看 [Vert.x 官网文档](https://vertx.io/docs/)

* 开箱即用的常用功能, 包括但不仅限于:

  * **sz-ebean** 基于**[Ebean](https://ebean.io/)**数据库访问工具类库.

    > 为很么采用Ebean作为数据库访问组件?
    >
    > * [vert.x 提供的JDBC的数据库访问组件](https://vertx.io/docs/#data_access) 不是 ORM 框架, 不符合大多数开发人员的习惯
    > * Ebean 是一套基于 JPA 规范的ORM框架, 并且对 Kotlin 有比较好的支持
    > * Ebean 对 **Active Record** 模式有着良好的支持, 参考: https://ebean.io/docs/setup/activerecord

  * **sz-api-doc** 自动生成api接口文档和测试页面

    > * 自动生成api接口文档和测试页面, 是 **sz框架** 生产力的核心体现.
    > * 是否有一套与代码实现保持一致的api接口文档和测试页面, 是前后端分离的开发模式能否顺利进行的关键保障.
    > * 代码即文档. 只引入一种注解**@Comment** 进行注释标注, 极低的心智负担
    > * api接口文档和测试页面是直接运行在http api server上的, 如果不需要, 直接在 route 里去掉路由定义即可
    > * 在开发过程中, 只要保证部署并实时更新的测试服务器提供给前端工程师. 就可以查看api接口文档, 进行前后端联调测试

  * 基于 Redis 的缓存实现

* 没有提供依赖注入, 开发人员可以选择自己喜欢的依赖注入框架, 这里列出2个采用kotlin语言开发的依赖注入框架, 供大家选择:

  * [Kodein-DI](https://github.com/Kodein-Framework/Kodein-DI)
  * [korinject](https://github.com/korlibs/korinject)

* 不提供微服务相关的组件, 不重复造车轮

  > 为什么?
  >
  > * 不想把 **sz** 变得更加复杂, 更加重型. 让 **sz** 就是一个单纯的 **快速搭建http api server** 的工具库.
  > * 如果我的应用就非常简单, 原型演示, 根本就不需要扯上微服务这个概念. 一个单机部署就行了
  > * 如果应用的规模真的很大, 那么微服务应该从架构和部署这2个方面来进行解决. 例如, 按照业务功能模块进行垂直划分, 分解成多个不同小规模的 http api server 或者 gRpc server 来实现各自的服务. 再通过k8s集群, 进行服务的部署, 服务自动发现, 服务治理, 服务负载均衡等等. 可以看到, k8s及其生态圈提供的各种AddOn组件, 已经包含了微服务架构的方方面面了. 我们不需要再重复造车轮了. 
  > * 各大云服务商, 都提供了 k8s 的支持. 做好 DevOps 才是重要的.

## 主要特点

* 工作运行在 **Java 环境**
* 开发语言采用 **Kotlin**, 首先从语言层面, 提高生产力(相比于原来Java语言的缺陷而言)
* 使用 Gradle 作为项目构建工具
* 基于 **Vert.x**, 面向 web 开发, 完全 **不需要 J2EE**, 没有Servlet, 没有web 容器
* 自动为定义的api接口, 生成接口文档和测试页面. 方便前端开发人员进行联调测试
* 通过 @[Comment](https://www.v2ex.com/member/Comment) 这一个注解. 在api接口的实现类, 字段, 方法, 方法参数 上, 框架会自动生成 api 接口文档和测试页面. 代码实现与文档保证一致性.
* 定义的api接口, 原生支持 **kotlin协程** 的编程方式
* 使用[**Ebean**](https://ebean.io/)这个ORM框架. sz框架对Ebean框架做了集成, 方便开发人员采用**kotlin协程**的编程方式来使用Ebean
* 站在巨人的肩膀上, sz框架充分利用了[**Vert.x**](https://vertx.io/docs/)这套工具库包含各个模块, 帮助开发人员实现从单机到集群,分布式等不同规模的应用.
* 丰富的[项目模板/脚手架样板工程](https://github.com/LoveInShenZhen/ProjectTemplates), 并且不断的还有新的脚手架样板工程被添加进来.


