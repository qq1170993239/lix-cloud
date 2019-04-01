package com.lix.cloud.sdk.dto;

import com.alibaba.fastjson.JSON;

/**
 * Created by lix on 2019/4/1.
 */
public class ResultDTO<T> {

    private String msg;
    private int code;
    private T result;
    private Throwable ex;

    private static final String SUCCESS_MSG = "执行成功！";
    private static final String FAILURE_MSG = "执行失败！";
    /**
     * 成功状态码
     */
    private static final int SUCCESS = 1;
    /**
     * 失败状态码
     */
    private static final int FAIL = 0;
    /**
     * 系统异常状态码
     */
    private static final int ERROR = -1;

    private ResultDTO(String msg, int code, T result, Throwable ex) {
        this.msg = msg;
        this.code = code;
        this.result = result;
        this.ex = ex;
    }

    public ResultDTO(String msg, T result) {
        this(msg, SUCCESS, result, null);
    }

    public ResultDTO(String msg) {
        this(msg, FAIL, null, null);
    }

    public ResultDTO(T result) {
        this(SUCCESS_MSG, SUCCESS, result, null);
    }

    public ResultDTO(String msg, Throwable ex) {
        this(msg, FAIL, null, ex);
    }

    /**
     * 只需要成功状态码时
     * @return
     */
    public static ResultDTO getSuccessResult() {
        return new ResultDTO(SUCCESS_MSG, SUCCESS, null, null);
    }
    /**
     * 只需要失败状态码时
     * @return
     */
    public static ResultDTO getFailResult() {
        return new ResultDTO(FAILURE_MSG, FAIL, null, null);
    }

    /**
     * 执行是否成功
     * @return
     */
    public Boolean isOk() {
        return this.code == SUCCESS;
    }

    /**
     * 输出JSON串
     * @return
     */
    @Override
    public String toString() {
        return JSON.toJSONString(this);
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getResult() {
        return result;
    }

    public void setResult(T result) {
        this.result = result;
    }

    public Throwable getEx() {
        return ex;
    }

    public void setEx(Throwable ex) {
        this.ex = ex;
    }
}
