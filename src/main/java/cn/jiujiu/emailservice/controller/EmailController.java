package cn.jiujiu.emailservice.controller;

import cn.jiujiu.emailservice.model.EmailRequest;
import cn.jiujiu.emailservice.model.VerificationCodeEmailRequest;
import cn.jiujiu.emailservice.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/email")
public class EmailController {


     @Autowired
     private EmailService emailService;

    @Value("${app.mail.template-name}")
     String templateName;

    @Value("${app.mail.verification-code-expiration}")
    Integer validityPeriod;


    @PostMapping("/send")
     public Mono<ResponseEntity<String>> sendEmail(@RequestBody Mono<EmailRequest> requestMono) {
         return requestMono
            .flatMap(request ->{
                if(request.getTo()==null || request.getTo().isEmpty()){
                    return Mono.just(ResponseEntity.badRequest().body("收件人不能为空"));
                }
                if(request.getContent()==null || request.getContent().isEmpty()){
                    request.setContent("");
                }
                if(request.getSubject()==null || request.getSubject().isEmpty()){
                    request.setSubject("");
                }

                return emailService.sendSimpleEmail(request.getTo(), request.getSubject(), request.getContent())
                    .map(success -> {
                        if (success) {
                            return ResponseEntity.ok("邮件发送成功");
                        } else {
                            return ResponseEntity.status(500).body("邮件发送失败");
                        }
                    });
            })
         .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request format: " + e.getMessage())));

     }

     @PostMapping("/sendHtml")
     public Mono<ResponseEntity<String>> sendHtmlEmail(@RequestBody Mono<VerificationCodeEmailRequest> requestMono) {
         return requestMono.flatMap(request->{
             if(request.getTo()==null||request.getTo().isEmpty())
             {
                 return Mono.just(ResponseEntity.badRequest().body("收件人不能为空"));
             }
             if(request.getVerificationCode()==null||request.getVerificationCode().isEmpty())
             {
                 return Mono.just(ResponseEntity.badRequest().body("验证码不能为空"));
             }
             Map<String,Object>templateDate=new HashMap<>();

             templateDate.put("username",request.getUsername() != null ? request.getUsername() : request.getTo());
             templateDate.put("verificationCode",request.getVerificationCode());
             templateDate.put("validityPeriod",validityPeriod);

             return emailService.sendTemplatedEmail(
                     request.getTo(),
                     request.getSubject(),
                     templateName,
                     templateDate
             ).map(success -> {
                 if (success) {
                     return ResponseEntity.ok("邮件发送成功");
                 } else {
                     return ResponseEntity.status(500).body("邮件发送失败");
                 }
             });
         })
         .onErrorResume(e->
                 Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid request format: " + e.getMessage()))
         );
     }
}
