package cn.jiujiu.emailservice.exception;

public class EmailSendFailureException extends RuntimeException {

    public EmailSendFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
