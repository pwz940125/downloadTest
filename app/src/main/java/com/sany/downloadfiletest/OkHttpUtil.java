package com.sany.downloadfiletest;

import android.util.Log;

import androidx.annotation.NonNull;

import com.alibaba.fastjson.JSON;
import com.sany.downloadfiletest.bean.FileInfo;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @ClassName: OkHttpUtil
 * @Description:
 * @Author: wuzhi.peng
 * @Date: 2022/3/25
 */
public class OkHttpUtil {
    private static ExecutorService executor = Executors.newFixedThreadPool(10);
    private static OkHttpClient getHttpClient(){
        File file = new File(App.context.getCacheDir(),"cache");
        Cache cache = new Cache(file,10*1024*1024);
        return new OkHttpClient.Builder()
                .connectTimeout(10L, TimeUnit.SECONDS)
                .readTimeout(15L,TimeUnit.SECONDS)
                .writeTimeout(15L,TimeUnit.SECONDS)
//                .addInterceptor(getLoggerInterceptor())
                .cache(cache)
                .build();
    }

    public static  void doHttpRequest(String url,HttpRequestCallBack callBack,ProgressListener listener){
        OkHttpClient client = getHttpClient().newBuilder().addNetworkInterceptor(chain -> {
            Response response = chain.proceed(chain.request());
            //这里将ResponseBody包装成我们的ProgressResponseBody
            return response.newBuilder()
                    .body(new ProgressResponseBody(response.body(),listener))
                    .build();
        }).build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        executor.execute(() -> call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call1, @NonNull IOException e) {
                callBack.onFailed(500,e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call1, @NonNull Response response) throws IOException {
                String value = response.body().string();
                if (JSON.parseObject(value).getIntValue("code") == 200) {
                    callBack.onSuccess(value);
                } else {
                    callBack.onFailed(JSON.parseObject(value).getIntValue("code"),JSON.parseObject(value).getString("message"));
                }
            }
        }));
    }


    public static  void doHttpRequestDownload(String url,HttpRequestCallBack callBack,ProgressListener listener){
        OkHttpClient client = getHttpClient().newBuilder().addNetworkInterceptor(chain -> {
            Response response = chain.proceed(chain.request());
            //这里将ResponseBody包装成我们的ProgressResponseBody
            return response.newBuilder()
                    .body(new ProgressResponseBody(response.body(),listener))
                    .build();
        }).build();
        Request request = new Request.Builder()
                .url(url)
                .build();
        Call call = client.newCall(request);
        executor.execute(() -> call.enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call1, @NonNull IOException e) {
                callBack.onFailed(500,e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call1, @NonNull Response response) throws IOException {
                callBack.onFileDownload(response.body().bytes());
            }
        }));
    }

    private static  Interceptor getLoggerInterceptor(){
        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d("wuzhi", message);
            }
        });
        interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        return interceptor;
    }

    public static void doHttpUpload(File file,HttpRequestCallBack callBack,ProgressListener listener){
        MultipartBody.Builder builder = new MultipartBody.Builder();
        builder.setType(MultipartBody.FORM);
        //第一个参数要与Servlet中的一致
        builder.addFormDataPart("file",file.getName(), RequestBody.create(MediaType.parse("application/octet-stream"),file));

        MultipartBody multipartBody = builder.build();

        Request request  = new Request.Builder().url("http://10.28.162.74:8080/upload").post(new ProgressRequestBody(multipartBody,listener)).build();
        getHttpClient().newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callBack.onFailed(500,e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String value = response.body().string();
                if (JSON.parseObject(value).getIntValue("code") == 200) {
                    callBack.onSuccess(value);
                } else {
                    callBack.onFailed(JSON.parseObject(value).getIntValue("code"),JSON.parseObject(value).getString("message"));
                }
            }
        });

    }
}
