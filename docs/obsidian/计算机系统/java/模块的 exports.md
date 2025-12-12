# 问题: 对指定 package 下的子package, 是否需要单独再增加 exports?

在 Java 模块系统（Module System，Java 9+）中，**是否需要为指定 package 下的子 package 单独增加 `exports`，取决于你是否希望外部模块访问这些子 package 中的类 / 接口**，核心规则如下：

### 1. 核心结论：`exports` 不递归，子 package 需显式声明

Java 模块的 `exports` 是**非递归**的 —— 即声明 `exports com.example.parent;` 仅允许外部模块访问 `com.example.parent` 包下的类，**其所有子 package（如 `com.example.parent.child`、`com.example.parent.child.sub`）都不会被自动导出**。

如果外部模块需要访问子 package 中的类，必须**单独为子 package 增加 `exports` 声明**。

### 2. 具体场景示例

假设模块结构如下：

plaintext

```plaintext
module com.example.myapp {
    // 导出父 package
    exports com.example.parent;

    // 子 package 需显式导出（若外部需访问）
    exports com.example.parent.child;
    exports com.example.parent.child.sub; // 多层子 package 同理
}
```

- 外部模块可直接访问 `com.example.parent.Xxx`（父包类）；
- 外部模块可访问 `com.example.parent.child.Yyy`（子包类，因显式导出）；
- 若未声明 `exports com.example.parent.child.sub;`，外部模块无法访问 `com.example.parent.child.sub.Zzz`（即使父包、祖父包已导出）。

### 3. 特殊情况：`exports ... to`（限定导出）

若需将子 package 仅导出给特定模块（而非所有模块），同样需显式声明，格式为：

java

```java
module com.example.myapp {
    // 父包导出给所有模块
    exports com.example.parent;

    // 子包仅导出给 com.example.client 模块
    exports com.example.parent.child to com.example.client;
}
```

### 4. 无需导出的场景

如果子 package 中的类仅在**模块内部使用**（不对外暴露），则无需声明 `exports`—— 模块内部的所有 package 之间默认可相互访问（无需额外配置）。

### 总结

|需求场景|是否需要单独 `exports` 子 package？|
|---|---|
|外部模块需访问子 package 中的类|是（显式声明 `exports 子包路径;`）|
|仅模块内部使用子 package|否（内部可直接访问，无需导出）|
|需限定子 package 仅导出给特定模块|是（显式声明 `exports 子包路径 to 目标模块;`）|

简单记：**`exports` 只作用于 “精确指定的 package”，子包不继承导出权限，需用则显式声明**。