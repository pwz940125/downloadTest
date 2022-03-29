package com.sany.downloadfiletest;

import android.util.Log;

import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

/**
 * @ClassName: ProgressRequestBody
 * @Description:
 * @Author: wuzhi.peng
 * @Date: 2022/3/29
 */
public class ProgressRequestBody extends RequestBody {
    private RequestBody requestBody;
    private ProgressListener mListener;
    private BufferedSink bufferedSink;
    public ProgressRequestBody(RequestBody body, ProgressListener listener) {
        requestBody = body;
        mListener = listener;

    }


    @Override
    public MediaType contentType() {
        return requestBody.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return requestBody.contentLength();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {

        if (bufferedSink==null){
            bufferedSink = Okio.buffer(sink(sink));
        }
        //写入
        requestBody.writeTo(bufferedSink);
        //刷新
        bufferedSink.flush();
    }

    private Sink sink(BufferedSink sink) {

        return new ForwardingSink(sink) {
            long bytesWritten = 0L;
            long contentLength = 0L;
            @Override
            public void write(Buffer source, long byteCount) throws IOException {
                super.write(source, byteCount);
                if (contentLength==0){
                    contentLength = contentLength();
                }
                bytesWritten += byteCount;
               mListener.onProgress(bytesWritten,contentLength,bytesWritten==contentLength);
            }
        };
    }


}
