package com.sany.downloadfiletest;

/**
 * @ClassName: HttpRequestCallBack
 * @Description:
 * @Author: wuzhi.peng
 * @Date: 2022/3/25
 */
public interface HttpRequestCallBack{
    void onSuccess(String body);
    void onFileDownload(byte[] bytes);
    void onFailed(int code,String message);
}
