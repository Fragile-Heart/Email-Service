package cn.jiujiu.emailservice.controller;

import cn.jiujiu.emailservice.model.ApiResponse;
import cn.jiujiu.emailservice.model.EmailRequest;
import cn.jiujiu.emailservice.model.VerificationCodeEmailRequest;
import cn.jiujiu.emailservice.service.EmailService;
import cn.jiujiu.emailservice.utils.ResponseUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
     public Mono<ResponseEntity<ApiResponse>> sendEmail(@RequestBody Mono<EmailRequest> requestMono) {
         return requestMono
            .flatMap(request ->{
                if(request.getTo()==null || request.getTo().isEmpty()){
                    return ResponseUtils.badRequest("Recipient cannot be empt");
                }
                if(request.getContent()==null || request.getContent().isEmpty()){
                    request.setContent("");
                }
                if(request.getSubject()==null || request.getSubject().isEmpty()){
                    request.setSubject("");
                }

                return emailService.sendSimpleEmail(request.getTo(), request.getSubject(), request.getContent())
                    .flatMap(success -> {
                        if (success) {
                            return ResponseUtils.ok("Email sent successfully");
                        } else {
                            return ResponseUtils.internalServerError("Failed to send email");
                        }
                    });
            })
         .onErrorResume(ResponseUtils::internalServerError);

     }

     @PostMapping("/sendHtml")
     public Mono<ResponseEntity<ApiResponse>> sendHtmlEmail(@RequestBody Mono<VerificationCodeEmailRequest> requestMono) {
         return requestMono.flatMap(request->{
             if(request.getTo()==null||request.getTo().isEmpty())
             {
                 return ResponseUtils.badRequest("Recipient cannot be empty");
             }
             if(request.getVerificationCode()==null||request.getVerificationCode().isEmpty())
             {
                 return ResponseUtils.badRequest("Verification code cannot be empty");
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
             ).flatMap(success -> {
                 if (success) {
                     return ResponseUtils.ok("Email sent successfully");
                 } else {
                     return ResponseUtils.internalServerError("Failed to send email");
                 }
             });
         })
         .onErrorResume(ResponseUtils::internalServerError);
     }
}
