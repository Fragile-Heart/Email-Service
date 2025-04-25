# Spring WebFlux 邮件服务

基于 Spring Boot 和 Spring WebFlux 构建的一个简单响应式邮件推送服务。它提供 REST API 接口，用于发送事务性邮件（例如找回密码、账号验证），并将阻塞式的邮件发送操作卸载到专门的线程池中处理。特别设计用于通过单个 HTML 模板发送不同目的的验证码邮件。

**核心特点：**

*   **响应式架构:** 利用 Spring WebFlux 和 Project Reactor 实现非阻塞 I/O。
*   **配置化发件人:** 发件人邮箱和 SMTP 服务器设置在 `application.properties` 文件中配置。
*   **集成 Spring Mail:** 利用 Spring Boot Starter Mail 简化邮件配置和发送。
*   **阻塞操作处理:** 在响应式流中，通过 Schedulers.boundedElastic() 将阻塞的 JavaMailSender 操作和 Thymeleaf 模板渲染卸载到专门的线程池，从而确保了响应式事件循环的非阻塞性，保证了服务的响应性。
*   **邮件模板支持:** 使用 Thymeleaf 模板引擎发送格式化的 HTML 邮件。
*   **灵活的模板变换:** 通用的验证码模板: 使用一个通用的 HTML 模板(verification_code.html)，通过动态数据支持多种验证码邮件场景；可定制模板: 允许配置使用自定义的 HTML 模板文件。

## 技术栈

*   Spring Boot
*   Spring WebFlux
*   Spring Mail
*   Thymeleaf
*   Project Reactor
*   Java (JDK 17+)
*   Maven

## 前置条件

*   Java Development Kit (JDK 17 或更高版本推荐)
*   已安装 Maven
*   一个具有 SMTP 访问权限的邮箱账户 (例如 Gmail, Outlook 等) 及其凭据 (用户名和密码；如果开启了两步验证/多重身份验证，强烈推荐使用应用专用密码)。

## 快速开始

### 1. 克隆仓库

```bash
git clone https://github.com/Fragile-Heart/Email-Service.git
cd <你的项目文件夹名称>
```

### 2. 配置说明

编辑 `src/main/resources/application.properties` 文件，填写你的邮件服务器详细信息。

```properties
# SMTP 服务器配置 (请替换为你的实际邮件服务器信息)
spring.mail.host=smtp.example.com      # 例如: smtp.gmail.com, smtp.office365.com, smtp.qq.com
spring.mail.port=587                   # SMTP 端口 (例如: 587 使用 TLS, 465 使用 SSL)
spring.mail.username=你的邮箱账号@example.com # 发件人邮箱账号 (用于SMTP认证)
spring.mail.password=你的密码或应用专用密码 # <-- 重要: 如果开启了两步验证，请务必使用应用专用密码!

# 推荐的邮件属性 (适用于端口 587 + TLS/STARTTLS)
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true # 启用 STARTTLS

# 如果使用端口 465 + SSL, 请注释掉上面一行，并取消注释下面几行:
# spring.mail.properties.mail.smtp.ssl.enable=true
# spring.mail.properties.mail.smtp.socketFactory.port=465
# spring.mail.properties.mail.smtp.socketFactory.class=javax.net.ssl.SSLSocketFactory

# 用于邮件中显示的"发件人"邮箱地址 (通常与 spring.mail.username 相同)
app.mail.sender-email=你的邮箱账号@example.com

# 服务器端口 (可选, 默认为 8080)
server.port=8080

# 如果要使用html模板发送邮件的接口，必须配置以下这两个，否则使用默认配置即可

# 验证码有效时间，单位为分钟
app.mail.verification-code-expiration=10
# HTML模板名字，在使用自己提供的HTML模板时使用
app.mail.template-name=verification_code
```

**🔐 安全警告:** 直接在 `application.properties` 文件中存储密码是**不安全**的，尤其是在生产环境中。请考虑使用更安全的方式，例如：
*   **环境变量:** Spring Boot 会自动读取与配置名对应的环境变量。例如，在运行应用程序时设置 `SPRING_MAIL_PASSWORD=你的密码`。
*   **Spring Cloud Config** 或 **HashiCorp Vault / 云秘密管理器:** 更专业的秘密管理方案。
*   **对于 Gmail/Outlook 等开启了两步验证的账户:** 你需要从你的邮件服务提供商的安全设置中生成**应用专用密码**来代替你的主账户密码。请查阅其官方文档。

### 3. 构建项目


```bash
mvn clean package
```

### 4. 运行应用程序

构建完成后，可以运行生成的 JAR 包:

如果使用 Maven:

```bash
java -jar target/<你的jar文件名>.jar
```

你也可以直接在 IDE 中运行主应用类 (`EmailserviceApplication.java`)。

## API 接口

服务提供了多个 POST 接口用于发送不同用途的验证码邮件。

### 1. 发送普通的邮件

用于发送纯文本邮件。

**接口:** `POST /api/email/send`

**请求 Header:**
*   `Content-Type: application/json`

**请求 Body (JSON):**

```json
{
    "to": "test@example.com",
    "subject": "test",
    "content": "hello world"
}
```

**说明:**
*   `to` 是收件人邮箱地址。
*   `subject` 是邮件主题
*   `content` 是邮件的内容

### 2. 发送验证码邮件

用于发送带有验证码的 HTML 邮件。

**接口:** `POST /api/email/sendHtml`

**请求 Header:**
*   `Content-Type: application/json`

**请求 Body (JSON):**

```json
{
    "to": "test@example.com",
    "subject": "找回密码",
    "verificationCode": "123456",
    "username": "玖玖"
}
```

**说明:**
*   `to`、`verificationCode`、`subject`是必需字段，分别是收件人邮箱地址，验证码和邮件主题。
*   `username` 是可选字段，如果提供，将用于邮件问候语的个性化，不提供则默认为收件人邮箱地址。
*   `app.mail.verification-code-expiration` 和 `app.mail.template-name` 配置项主要用于 `/api/email/sendHtml` 接口：
    *   `app.mail.verification-code-expiration`: 设置验证码的有效时间。
    *   `app.mail.template-name`: 指定 `/api/email/sendHtml` 接口使用的 Thymeleaf 模板文件名（不含 `.html` 后缀）。默认使用 `verification_code` 模板。如果您使用自定义模板，请将 HTML 文件放在 `src/main/resources/templates` 目录下，并将此配置修改为您的文件名。


## 邮件模板

邮件模板文件存放在 `src/main/resources/templates/` 目录下。主要使用的是 `verification_code.html` 文件，它是一个用于发送各种验证码的通用 HTML 模板。

*   该模板利用 Thymeleaf 语法引用动态内容。`/api/email/sendHtml` 接口会根据请求参数（如验证码、用户名等）构建一个包含这些动态数据的 Map，并将其作为数据源传递给 Thymeleaf 模板引擎进行渲染。邮件的具体内容（如标题、用途描述、主题等）由此 API 接口的调用方决定，通过构造不同的数据 Map 来填充模板中的变量。

    以下是对这些变量的解读，如果想要修改模板，可参考这些变量的作用进行修改：
    *   `${subject}`: 邮件主题行。
    *   `${username}`: 收件人的姓名或用户名。
    *   `${verificationCode}`: 实际的验证码。
    *   `${instructions}`: (可选) 额外的说明文字。

API 接口负责构造包含正确变量值的 `templateData` Map，并将其传递给邮件服务进行模板渲染。这样，同一个模板文件就可以通过不同的数据呈现不同的邮件内容。
## License

This project is licensed under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0) - see the [LICENSE](LICENSE) file for details.

## 贡献

欢迎贡献! 如果你发现了 Bug 或有改进的想法，请提交 Issue 或 Pull Request。
