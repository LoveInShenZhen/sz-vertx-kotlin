

# 一、Java Module概述

## 1.1 什么是Java Module？

Java Module（模块）是Java 9引入的核心新特性，旨在解决传统Java应用中“类路径地狱（Classpath Hell）”问题，同时提供更强大的封装性、依赖管理和模块化部署能力。

本质上，一个Java模块是一组相关的Java类、资源文件以及一个描述模块信息的`module-info.java`文件的集合。模块可以明确声明自己依赖的其他模块，以及对外暴露的包（其他模块可访问的包），未暴露的包则完全封装，即使通过反射也无法访问（除非显式授权）。

## 1.2 引入Java Module的核心目的

- **解决类路径问题**：传统类路径是“扁平”结构，多个JAR包中同名类会导致冲突；模块通过明确的依赖关系和唯一模块名，避免类冲突。

- **强化封装性**：传统`public`修饰的类在整个应用中都可访问，模块可精确控制哪些包对外暴露，未暴露包仅模块内部可见。

- **简化依赖管理**：模块显式声明依赖，编译和运行时可自动校验依赖完整性，避免“缺失类”错误。

- **优化JRE体积**：JDK自身被拆分为多个模块（如`java.base`、`java.lang`），可通过“模块化运行”只加载所需模块，减少资源占用（如嵌入式场景）。

## 1.3 Java Module的核心术语

|术语|定义|
|---|---|
|模块（Module）|包含`module-info.java`的JAR包或目录，是模块化的基本单位|
|模块描述符|即`module-info.java`，定义模块名、依赖、暴露包等信息|
|模块路径（ModulePath）|存放模块的路径，替代传统类路径，JVM从这里加载模块|
|导出包（Exported Package）|模块对外暴露的包，其他模块可通过`requires`访问|
|依赖模块（Required Module）|当前模块运行所依赖的其他模块，通过`requires`声明|
|服务提供者（Service Provider）|实现特定服务接口的模块，通过`provides...with`声明|
|服务消费者（Service Consumer）|使用特定服务的模块，通过`uses`声明|
# 二、模块描述符（module-info.java）详解

`module-info.java`是模块的“身份证”，必须放在模块的根目录下（即包的最外层），语法简洁但包含模块的核心配置。以下是完整语法及关键关键字解析。

## 2.1 基本结构

```java

// 模块名（唯一标识，建议采用反向域名命名，如com.example.demo）
module com.example.demo {
    // 1. 声明依赖的模块
    requires java.base; // 依赖基础模块（默认隐式依赖，可省略）
    requires transitive com.example.utils; // 传递性依赖，依赖当前模块的模块自动依赖utils
    
    // 2. 导出对外暴露的包（其他模块可访问）
    exports com.example.demo.service; // 导出service包
    exports com.example.demo.model to com.example.client; // 定向导出，仅client模块可访问
    
    // 3. 声明提供的服务（服务提供者）
    provides com.example.demo.service.UserService 
        with com.example.demo.service.impl.UserServiceImpl;
    
    // 4. 声明使用的服务（服务消费者）
    uses com.example.demo.service.UserService;
    
    // 5. 开放包（允许其他模块通过反射访问，突破封装）
    opens com.example.demo.internal to java.base, com.example.test;
}

```

## 2.2 核心关键字解析

### 2.2.1 模块声明：module

语法：`module 模块名 { ... }`

模块名必须唯一，推荐遵循“反向域名”规则（如`org.springframework.core`），避免冲突。

### 2.2.2 依赖声明：requires

用于声明当前模块依赖的其他模块，核心变种：

- **基础依赖**：`requires 模块名;` 表示当前模块依赖指定模块，且依赖是非传递的（依赖当前模块的模块不会自动依赖该模块）。

- **传递性依赖**：`requires transitive 模块名;` 若A模块依赖B模块且用`transitive`，则依赖A的C模块会自动依赖B（适用于“公共依赖”场景，如工具类模块）。

- **静态依赖**：`requires static 模块名;` 仅在编译时依赖，运行时不强制要求该模块存在（适用于“可选功能”场景，如日志适配模块）。

### 2.2.3 包导出：exports

用于控制模块对外暴露的包，未导出的包仅模块内部可访问（即使是`public`类），是模块封装性的核心体现：

- **普通导出**：`exports 包名;` 所有依赖当前模块的模块都可访问该包下的`public`类和接口。

- **定向导出**：`exports 包名 to 模块1, 模块2;` 仅指定的模块可访问该包，进一步精细化控制访问范围（适用于“模块间协作但不对外公开”场景）。

### 2.2.4 服务相关：provides & uses

实现“服务发现”机制，解耦服务接口和实现：

- **provides（服务提供者）**：`provides 服务接口 with 服务实现类;` 声明当前模块提供某服务接口的实现，需确保实现类是`public`且无参构造器可访问。

- **uses（服务消费者）**：`uses 服务接口;` 声明当前模块需要使用某服务接口，可通过`ServiceLoader.load(服务接口.class)`动态加载所有提供该服务的实现。

### 2.2.5 反射开放：opens

传统Java中，反射可访问任何类（即使是`private`成员），模块默认禁止对未导出包的反射访问，`opens`用于显式开放反射权限：

- **普通开放**：`opens 包名;` 允许所有模块对该包下的类进行反射访问（包括私有成员）。

- **定向开放**：`opens 包名 to 模块1, 模块2;` 仅允许指定模块对该包进行反射访问（适用于测试框架、ORM框架等场景，如JUnit需要反射访问测试类）。

- **开放模块**：`open module 模块名 { ... }` 声明整个模块为“开放模块”，所有包都允许反射访问（不推荐，除非是框架类模块）。

# 三、Java Module使用流程（实战步骤）

以“多模块项目”为例，演示从创建模块、编写描述符到编译运行的完整流程。假设项目包含3个模块：

- `com.example.utils`：工具类模块，提供字符串处理工具

- `com.example.service`：服务模块，依赖utils模块，提供用户服务

- `com.example.app`：应用主模块，依赖service模块，作为入口

## 3.1 项目结构搭建

模块化项目推荐采用“按模块划分目录”的结构，每个模块有独立的源码目录和`module-info.java`：

```plaintext

java-module-demo/
├── com.example.utils/          // 工具模块目录
│   └── src/
│       └── main/
│           └── java/
│               ├── module-info.java  // 工具模块描述符
│               └── com/
│                   └── example/
│                       └── utils/
│                           └── StringUtils.java  // 工具类
├── com.example.service/        // 服务模块目录
│   └── src/
│       └── main/
│           └── java/
│               ├── module-info.java  // 服务模块描述符
│               └── com/
│                   └── example/
│                       └── service/
│                           ├── UserService.java  // 服务接口
│                           └── impl/
│                               └── UserServiceImpl.java  // 服务实现
└── com.example.app/            // 主应用模块目录
    └── src/
        └── main/
            └── java/
                ├── module-info.java  // 主模块描述符
                └── com/
                    └── example/
                        └── app/
                            └── App.java  // 主类（入口）

```

## 3.2 编写各模块代码

### 3.2.1 工具模块（com.example.utils）

1. 编写工具类`StringUtils.java`：

```java

package com.example.utils;

public class StringUtils {
    // 对外提供的工具方法
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }
    
    // 未导出包的内部方法（仅模块内可见）
    static String format(String str) {
        return str.trim().toUpperCase();
    }
}

```

2. 编写模块描述符`module-info.java`：

```java

// 工具模块，导出utils包供其他模块使用
module com.example.utils {
    exports com.example.utils; // 导出工具包
}

```

### 3.2.2 服务模块（com.example.service）

1. 编写服务接口`UserService.java`：

```java

package com.example.service;

import com.example.utils.StringUtils;

public interface UserService {
    boolean validateUsername(String username);
}

```

2. 编写服务实现`UserServiceImpl.java`：

```java

package com.example.service.impl;

import com.example.service.UserService;
import com.example.utils.StringUtils;

public class UserServiceImpl implements UserService {
    @Override
    public boolean validateUsername(String username) {
        // 调用工具模块的方法
        return !StringUtils.isEmpty(username) && username.length() >= 6;
    }
}

```

3. 编写模块描述符`module-info.java`：

```java

// 服务模块，依赖工具模块，导出service包
module com.example.service {
    requires com.example.utils; // 依赖工具模块
    exports com.example.service; // 导出服务接口包
    provides com.example.service.UserService with com.example.service.impl.UserServiceImpl; // 提供服务实现
}

```

### 3.2.3 主应用模块（com.example.app）

1. 编写主类`App.java`：

```java

package com.example.app;

import com.example.service.UserService;
import java.util.ServiceLoader;

public class App {
    public static void main(String[] args) {
        // 方式1：直接创建实现类（耦合实现）
        // UserService userService = new UserServiceImpl();
        
        // 方式2：通过服务发现加载实现（解耦）
        ServiceLoader<UserService> serviceLoader = ServiceLoader.load(UserService.class);
        UserService userService = serviceLoader.findFirst().orElseThrow(() -> new RuntimeException("未找到UserService实现"));
        
        String username = "test123";
        boolean valid = userService.validateUsername(username);
        System.out.println("用户名" + username + (valid ? "有效" : "无效"));
    }
}

```

2. 编写模块描述符`module-info.java`：

```java

// 主应用模块，依赖服务模块，使用服务接口
module com.example.app {
    requires com.example.service; // 依赖服务模块
    uses com.example.service.UserService; // 声明使用UserService服务
}

```

## 3.3 编译与运行（命令行方式）

模块化项目的编译和运行需指定“模块路径”（`--module-path`，简称`-p`），替代传统的类路径（`-classpath`）。

### 3.3.1 编译各模块

在项目根目录（`java-module-demo`）下执行以下命令，将编译后的类文件输出到`out`目录：

```bash

# 1. 编译工具模块
javac -d out/com.example.utils src/main/java/module-info.java src/main/java/com/example/utils/StringUtils.java

# 2. 编译服务模块（需指定工具模块的路径）
javac -d out/com.example.service --module-path out src/main/java/module-info.java src/main/java/com/example/service/*.java src/main/java/com/example/service/impl/*.java

# 3. 编译主应用模块（需指定服务模块的路径）
javac -d out/com.example.app --module-path out src/main/java/module-info.java src/main/java/com/example/app/App.java

```

### 3.3.2 运行主应用

通过`--module`（简称`-m`）指定主模块和主类（格式：`模块名/主类全路径`）：

```bash

java --module-path out -m com.example.app/com.example.app.App

```

运行结果：`用户名test123有效`

## 3.4 IDEA中使用模块（推荐）

IDEA对Java Module有完善支持，步骤如下：

1. 创建项目时选择“Java”，并勾选“Create module-info.java”（若未勾选，后续可右键源码目录新建）。

2. 右键项目→“New”→“Module”，创建多个子模块，自动生成模块结构。

3. 模块间依赖：右键模块→“Open Module Settings”→“Dependencies”→“+”→选择依赖的模块。

4. 运行：右键主类→“Run”，IDEA自动处理模块路径和依赖。

# 四、高级特性

## 4.1 自动模块（Automatic Module）

对于没有`module-info.java`的传统JAR包，JVM会将其视为“自动模块”，用于兼容旧版本依赖：

- 模块名：默认由JAR包名推导（去掉版本号、横线等，如`commons-lang3-3.14.0.jar`的自动模块名为`commons.lang3`），也可通过JAR包Manifest文件的`Automatic-Module-Name`属性指定。

- 特性：自动模块会导出所有包，且依赖所有其他模块（包括自动模块和显式模块），封装性较差，仅建议过渡期使用。

## 4.2 模块层（Module Layer）

模块层是JVM中模块的“组织单元”，用于实现“动态加载和卸载模块”（传统类路径无法卸载类）：

- **Boot Layer**：启动层，包含JDK的核心模块（如`java.base`）和主模块，启动后无法卸载。

- **自定义层**：通过`ModuleLayer.Controller`创建，可动态加载模块，用完后卸载（适用于插件化应用场景，如IDE的插件加载）。

示例代码（动态加载模块）：

```java

// 1. 创建模块路径
Path modulePath = Paths.get("out");
ModuleFinder finder = ModuleFinder.of(modulePath);

// 2. 定义模块层（依赖启动层）
ModuleLayer parent = ModuleLayer.boot();
ModuleLayer layer = parent.defineModulesWithOneLoader(finder, ClassLoader.getSystemClassLoader());

// 3. 加载模块中的类并使用
Class<?> clazz = layer.findModule("com.example.utils")
    .flatMap(m -> m.getClassLoader().loadClass("com.example.utils.StringUtils"))
    .orElseThrow();
Method method = clazz.getMethod("isEmpty", String.class);
boolean result = (boolean) method.invoke(null, "");

```

## 4.3 模块化JAR与JPMS打包

通过`jlink`工具可将模块及其依赖打包为“自定义运行时镜像”（仅包含所需模块的JRE），大幅减小部署体积：

```bash

# 语法：jlink --module-path 模块路径 --add-modules 主模块 --output 输出目录
jlink --module-path $JAVA_HOME/jmods:out --add-modules com.example.app --output app-image

# 运行镜像中的应用
app-image/bin/java -m com.example.app/com.example.app.App

```

说明：`$JAVA_HOME/jmods`是JDK模块目录，`--add-modules`指定要包含的模块，jlink会自动收集所有依赖模块。

# 五、常见问题与解决方案

## 5.1 模块找不到（Module not found）

- 原因1：模块路径未指定或错误，编译/运行时未通过`--module-path`指定模块所在目录。

- 原因2：模块名拼写错误，需确保`requires`的模块名与目标模块的`module-info.java`中声明的一致。

- 原因3：自动模块名推导错误，可通过Manifest文件指定`Automatic-Module-Name`。

## 5.2 包不可访问（Package is not visible）

- 原因：目标模块未导出该包，或当前模块不在定向导出的范围内。

- 解决方案：在目标模块的`module-info.java`中通过`exports`导出该包，或定向导出给当前模块。

## 5.3 反射访问被拒绝（IllegalAccessException）

- 原因：要反射的包未通过`opens`开放给当前模块。

- 解决方案：在目标模块中通过`opens`开放该包，或声明为开放模块。

## 5.4 依赖冲突（Duplicate module）

- 原因：模块路径中存在两个同名模块（如两个不同版本的`com.example.utils`）。

- 解决方案：确保模块名唯一，通过版本管理工具（如Maven）控制依赖版本，避免重复引入。

# 六、总结与最佳实践

## 6.1 核心总结

- Java Module的核心是`module-info.java`，通过依赖声明和包导出实现“强封装”和“清晰依赖”。

- 模块路径（ModulePath）替代类路径，解决类冲突和依赖模糊问题。

- 服务发现（provides/uses）实现接口与实现解耦，模块层支持动态加载卸载。

## 6.2 最佳实践

1. **模块名规范**：采用反向域名命名，确保唯一（如`com.company.project.module`）。

2. **最小暴露原则**：仅导出必要的包，未对外提供的功能一律不导出（如内部工具类、实现类）。

3. **谨慎使用传递性依赖**：仅对“公共基础模块”使用`requires transitive`，避免依赖膨胀。

4. **避免开放模块**：尽量通过`opens`定向开放反射权限，而非声明整个模块为开放模块。

5. **兼容旧依赖**：对无模块描述符的JAR包，优先使用其官方提供的模块化版本，无则使用自动模块过渡。

6. **配合构建工具**：Maven 3.6+、Gradle 6+均支持Java Module，通过配置`module-info.java`和依赖，自动处理编译和打包。

# 七、参考资料

- Oracle官方文档：[Java Platform, Standard Edition Modules Specification](https://docs.oracle.com/en/java/javase/21/docs/api/java.base/java/lang/module/package-summary.html)

- 《Java 9模块化开发实战》

- IntelliJ IDEA官方指南：[Working with Modules](https://www.jetbrains.com/help/idea/working-with-modules.html)
> （注：文档部分内容可能由 AI 生成）