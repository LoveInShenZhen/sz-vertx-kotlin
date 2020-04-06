# sz-vertx-kotlin 后端快速开发框架

详情请查看[网站文档](http://loveinshenzhen.github.io/#/)

下文将使用 **sz框架** 简称 

## 什么是 sz框架

* **sz框架** 是一套在**前后端分离**的开发模式下, 专注于**后端http api接口**的快速开发框架 

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



# About Me

我是一名四十多岁的大龄程序员, 目前失业在家中. 希望有好心的小伙伴可以帮忙推荐和介绍一下, 不胜感激!

[个人简历](https://kklongming.github.io/resume/longming_2020.pdf)

# 项目捐助

有钱捧个钱场, 没钱捧个人场

| 通过微信捐助                                                 | 通过支付宝捐助                                               |
| ------------------------------------------------------------ | ------------------------------------------------------------ |
| ![微信](https://kklongming.github.io/res/images/wechat_donate.png) | ![支付宝](https://kklongming.github.io/res/images/alipay_donate.png) |


