package com.lix.cloud.sdk.exception;

/**
 * 服务执行时异常
 * @author: lix
 * @Date: 2019/4/1
 */
public class ServerException extends RuntimeException {

    private String msg;

    public ServerException() {
        super();
    }

    public ServerException(String message) {
        super(message);
        this.msg = message;
    }

    public ServerException(String message, Throwable cause) {
        super(message, cause);
        this.msg = message;
    }

    public ServerException(Throwable cause) {
        super(cause);
    }

    protected ServerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
