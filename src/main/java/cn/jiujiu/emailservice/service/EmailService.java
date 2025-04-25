package cn.jiujiu.emailservice.service;


import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${app.mail.sender-email}")
    private String senderEmail;
    @Autowired
    private TemplateEngine templateEngine;

    /**
     * 发送简单邮件
     * @param to      收件人
     * @param subject 邮件主题
     * @param text    邮件内容
     * @return 是否发送成功
     */
    public Mono<Boolean> sendSimpleEmail(String to, String subject, String text) {

        return Mono.fromCallable(()->{
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, "UTF-8");

                helper.setFrom(senderEmail);
                helper.setTo(to);
                helper.setSubject(subject);
                helper.setText(text, false); // false 表示纯文本

                mailSender.send(message);

                return true;
            }catch (MessagingException e) {
                log.error("Error creating email message to {}: {}", to, e.getMessage(), e);
                throw new RuntimeException("Error creating email message", e); // Propagate as unchecked
            } catch (MailException e) {
                log.error("Error sending email to {}: {}", to, e.getMessage(), e);
                throw new RuntimeException("Error sending email", e); // Propagate as unchecked
            } catch (Exception e) {
                log.error("An unexpected error occurred while sending email to {}: {}", to, e.getMessage(), e);
                throw new RuntimeException("Unexpected error during email sending", e); // Propagate as unchecked
            }
        })
        .subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(e -> Mono.just(false));
    }

    /**
     * 发送带html模板的邮件
     * @param to 收件人邮箱
     * @param subject 邮件主题
     * @param templateName 模板文件名 (不含 .html 后缀，文件存于 resources/templates 目录下)
     * @param templateData 模板所需的数据 (key-value map, 包含 username, verificationCode, heading, purposeText 等)
     * @return 是否发送成功
     */
    public Mono<Boolean> sendTemplatedEmail(String to, String subject, String templateName, Map<String, Object> templateData) {

        return Mono.fromCallable(() -> {
            try {
                MimeMessage message = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

                helper.setFrom(senderEmail);
                helper.setTo(to);
                helper.setSubject(subject);

                // 使用Thymeleaf渲染模板
                Context context = new Context();
                if (templateData != null) {
                    templateData.forEach(context::setVariable);
                }

                // 添加主题作为模板变量
                context.setVariable("subject", subject);

                // 渲染模板获取HTML内容
                String htmlContent = templateEngine.process(templateName, context);

                helper.setText(htmlContent, true);
                mailSender.send(message);

                log.info("成功发送模板邮件至：{}", to);
                return true;
            } catch (MessagingException e) {
                log.error("创建邮件消息时出错，收件人：{}: {}", to, e.getMessage(), e);
                throw new RuntimeException("创建邮件消息出错", e);
            } catch (MailException e) {
                log.error("发送邮件时出错，收件人：{}: {}", to, e.getMessage(), e);
                throw new RuntimeException("发送邮件出错", e);
            } catch (Exception e) {
                log.error("发送邮件过程中发生意外错误，收件人：{}: {}", to, e.getMessage(), e);
                throw new RuntimeException("发送邮件过程中发生意外错误", e);
            }
        }).subscribeOn(Schedulers.boundedElastic())
        .onErrorResume(e -> {
            log.error("邮件发送失败: {}", e.getMessage());
            return Mono.just(false);
        });
    }
}
