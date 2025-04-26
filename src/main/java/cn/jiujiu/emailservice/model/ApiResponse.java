package cn.jiujiu.emailservice.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApiResponse implements Serializable {


    private int code;
    private String message; // 响应消息 (例如: "Success", "Bad Request")

    // 私有构造函数，强制使用静态工厂方法创建实例
    private ApiResponse(int code, String message) {
        this.code = code;
        this.message = message;
    }

    /**
     * 成功响应 (code 200, 无数据)
     * @param message 成功消息
     * @return 成功响应实例
     */
    public static ApiResponse success(String message) {
        return new ApiResponse(200, message);
    }

    /**
     * 客户端错误响应 (code 400 - Bad Request)
     * @param message 错误消息 (例如: "参数无效")
     * @return 错误响应实例
     */
    public static ApiResponse clientError(String message) {
        return new ApiResponse(400, message);
    }


    /**
     * 服务器错误响应 (code 500 - Internal Server Error)
     * @param message 错误消息
     * @return 错误响应实例
     */
    public static ApiResponse serverError(String message) {
        return new ApiResponse(500, message);
    }
}