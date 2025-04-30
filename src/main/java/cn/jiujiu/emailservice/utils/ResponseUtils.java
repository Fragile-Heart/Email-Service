package cn.jiujiu.emailservice.utils;

import cn.jiujiu.emailservice.model.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import reactor.core.publisher.Mono;

@Slf4j
public class ResponseUtils {

    /**
     * 生成成功响应 (HTTP 200 OK)
     * @param message 成功消息
     * @return Mono<ResponseEntity<ApiResponse<Void>>>
     */
    public static Mono<ResponseEntity<ApiResponse>> ok(String message) {
        return Mono.just(ResponseEntity.ok(ApiResponse.success(message)));
    }

    /**
     * 生成客户端错误响应 (HTTP 400 Bad Request)
     * @param message 错误消息
     * @return Mono<ResponseEntity<ApiResponse<Void>>>
     */
    public static Mono<ResponseEntity<ApiResponse>> badRequest(String message) {
        return Mono.just(ResponseEntity.badRequest().body(ApiResponse.clientError(message)));
    }

    /**
     * 生成服务器错误响应 (HTTP 500 Internal Server Error, 自定义消息)
     * @param message 错误消息
     * @return Mono<ResponseEntity<ApiResponse<Void>>>
     */
    public static Mono<ResponseEntity<ApiResponse>> internalServerError(String message) {
        return Mono.just(ResponseEntity.status(500).body(ApiResponse.serverError(message)));
    }


    /**
     * 生成服务器错误响应 (HTTP 500 Internal Server Error, 默认消息)
     * @param throwable 异常
     * @return Mono<ResponseEntity<ApiResponse<Void>>>
     */
    public static Mono<ResponseEntity<ApiResponse>> internalServerError(Throwable throwable) {
        // 可以在这里根据 throwable 类型生成更详细或不同的错误消息

        log.error(throwable.getMessage());

        return internalServerError(throwable.getMessage());
    }
}
