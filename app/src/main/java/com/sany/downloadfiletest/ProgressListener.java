package com.sany.downloadfiletest;

/**
 * @ClassName: ProgressListener
 * @Description:
 * @Author: wuzhi.peng
 * @Date: 2022/3/25
 */
public interface ProgressListener {
    void onProgress(long currentBytes,long totalBytes,boolean done);
}
