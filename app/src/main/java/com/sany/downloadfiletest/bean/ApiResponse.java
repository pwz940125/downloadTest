package com.sany.downloadfiletest.bean;

/**
 * @ClassName: Response
 * @Description:
 * @Author: wuzhi.peng
 * @Date: 2022/3/25
 */
public class ApiResponse<T> {
    private int code;
    private String message;
    private T data;

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
