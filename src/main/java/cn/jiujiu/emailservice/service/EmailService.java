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
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@Slf4j
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;
    @Value("${app.mail.sender-email}")
    private String senderEmail;

    /**
     * 发送邮件
     *
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
     * 发送 HTML 邮件
     *
     * @param to      收件人
     * @param subject 邮件主题
     * @param text    邮件内容
     * @return 是否发送成功
     */
    public Mono<Boolean> sendHtmlEmail(String to, String subject, String text) {

        return Mono.just(true);
    }
}
