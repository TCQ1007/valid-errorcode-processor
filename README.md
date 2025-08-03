# error-code-processor

一个用于 Java 项目的注解处理器，帮助开发者规范和管理错误码。

# 功能特性

- 自动校验错误码的唯一性和规范性
- 支持自定义错误码前缀和格式
- 生成错误码文档，便于维护和查阅
- 与主流构建工具（如 Gradle、Maven）兼容

## 快速开始

### 1. 引入依赖

推荐通过 Maven Central 仓库引入依赖：

**Maven 配置：**

```xml
<dependency>
    <groupId>io.github.tcq1007</groupId>
    <artifactId>valid-error-code-processor</artifactId>
    <version>1.0.2</version>
</dependency>
```

**Gradle 配置：**

```groovy
dependencies {
    annotationProcessor 'io.github.tcq1007:valid-error-code-processor:1.0.2'
}
```

更多信息可参考：[Maven Central 仓库页面](https://central.sonatype.com/artifact/io.github.tcq1007/valid-error-code-processor/overview)

### 2. 使用注解

在你的错误码枚举或类上使用相关注解。例如：

```java
@ValidErrorCode
@Getter
@AllArgsConstructor
public enum ApiError {
   OK(0,"ok"),
   Error(11223445,"ok"),
   Error2(11223446,"ok"),
   ;
   private final int code;
   private final String message;
}
```

### 3. 编译校验

构建项目时，注解处理器会自动校验错误码的唯一性和规范性。

### 4. 生成文档

编译后会在 `build/docs/javadoc` 目录下生成错误码文档，便于查阅和维护。

## 配置说明

可通过 `config.properties` 或环境变量配置以下参数：

- `GPG_PASSPHRASE`：GPG 签名密码
- `JRELEASER_MAVENCENTRAL_USERNAME`：MavenCentral 用户名
- `JRELEASER_MAVENCENTRAL_PASSWORD`：MavenCentral 密码
- `JRELEASER_GITHUB_TOKEN`：GitHub Token

## 贡献指南

欢迎提交 issue 和 PR 参与项目共建。

## 开发计划

### 待实现功能

1. 错误码唯一性校验
   - [ ] 实现跨模块的错误码唯一性检查
   - [ ] 添加错误码格式验证规则配置
   - [ ] 支持自定义错误码冲突检测策略

## 发布说明

### JReleaser

本项目使用 [JReleaser](https://jreleaser.org/) 进行发布管理，支持：

- 自动化版本发布流程
- Maven Central 仓库发布
- 生成更新日志
- GPG 签名
- GitHub Release 创建

要进行发布，请确保：

1. 配置了正确的 GPG 密钥
2. 设置了必要的环境变量（见配置说明）
3. 更新了版本号（build.gradle 中的 version）
4. 在用户目录下创建 `.jreleaser` 文件夹，并创建 `config.properties` 文件，在文件中配置如下内容

```properties
JRELEASER_GPG_PASSPHRASE=xxx
JRELEASER_MAVENCENTRAL_USERNAME=xxx
JRELEASER_MAVENCENTRAL_PASSWORD=xxx
# 这一条随便配点东西不然要报错
JRELEASER_GITHUB_TOKEN=xxx
```
5. 先执行gradle的publish 任务再执行发布：
```bash
./gradlew jreleaserDeploy
```
之所以这样是 jrelease任务中配置的 `stagingRepository('build/staging-deploy')` 这个目录是由publish 任务生成的目录和产物文件存放的地方，从 publishing的配置中也可以看出它配置了一个
```groovy
repositories {
   maven {
      url = layout.buildDirectory.dir('staging-deploy')
   }
}
```
## License

本项目基于 Apache License 2.0 协议开源。

完整的协议内容请参见 [LICENSE](LICENSE) 文件。Apache License 2.0 是一个宽松的开源协议，允许：

- 商业使用
- 修改
- 分发
- 专利授权
- 私人使用

使用本项目时，需要：

- 保留原始协议声明
- 声明重大修改
- 保留作者版权信息
