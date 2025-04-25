package cn.jiujiu.emailservice.model;


import lombok.Data;

@Data
public class VerificationCodeEmailRequest {

        private String to; // 收件人邮箱
        private String username; // 收件人用户名 (可选，默认为收件人邮箱)
        private String verificationCode; // 验证码
        private String subject; // 邮件主题

}
