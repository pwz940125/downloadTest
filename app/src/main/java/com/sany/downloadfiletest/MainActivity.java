package com.sany.downloadfiletest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.listener.OnItemChildClickListener;
import com.chad.library.adapter.base.viewholder.BaseViewHolder;
import com.sany.downloadfiletest.bean.ApiResponse;
import com.sany.downloadfiletest.bean.FileInfo;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    private RecyclerView mRecyclerView;
    private List<FileInfo> mFileList;
    private Button mSelectBt;
    private Button mUploadBt;
    private TextView mSelectFile;
    private File mUploadFile;
    private ProgressDialog progressDialog;
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
        }
        FileUtil.createSaveFileFolder(this);
        initView();
        progressDialog = new ProgressDialog(this);
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setMax(100);
        progressDialog.setTitle("上传进度稍纵即逝");
    }

    private void initView(){
        mSelectBt = findViewById(R.id.bt_select_file);
        mUploadBt = findViewById(R.id.bt_upload);
        mSelectFile = findViewById(R.id.upload_file_name);
        mSelectBt.setOnClickListener(this);
        mUploadBt.setOnClickListener(this);
        mRecyclerView = findViewById(R.id.recycler);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this,DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mAdapter);
        getFileListFromNetWork();
        mAdapter.addChildClickViewIds(R.id.download);
        mAdapter.setOnItemChildClickListener((adapter, view, position) -> {
            if (view.getId() == R.id.download) {
                downLoadFile(mFileList.get(position).getName(),position);
            }
        });
    }


    private BaseQuickAdapter<FileInfo, BaseViewHolder> mAdapter = new BaseQuickAdapter<FileInfo, BaseViewHolder>(R.layout.layout_recycler_item) {
        @Override
        protected void convert(BaseViewHolder holder, FileInfo s) {
            holder.setText(R.id.name,s.getName());
            long size = s.getSize();
            String realSize ;
            if (size < 1024) {
                realSize = size+"b";
            } else if (size<1024*1024){
                realSize = (size/1024)+"kb";
            } else if (size<1024*1024*1024) {
                realSize = (size/1024/1024)+"Mb";
            } else {
                realSize = "有点大";
            }
            holder.setText(R.id.size,realSize);
            ProgressBar progressBar = holder.getView(R.id.progress);
            progressBar.setVisibility(s.getDownloadState()==1?View.VISIBLE:View.GONE);
            if (s.getdTotalSize()!=0L)
            progressBar.setProgress((int) ((s.getdCurrentSize()*100)/s.getdTotalSize()));
            holder.setText(R.id.download,s.getDownloadState()==0?"下载":s.getDownloadState()==1?"下载中":"完成");
            Log.d("wuzhi"," ssss " + s.getDownloadState());

        }
    };

    /**
     * 获取可下载的文件
     */
    private void getFileListFromNetWork(){
        OkHttpUtil.doHttpRequest("http://10.28.162.74:8080/getFileList", new HttpRequestCallBack() {
            @SuppressLint("NotifyDataSetChanged")
            @Override
            public void onSuccess(String body) {
                runOnUiThread(() -> {
                    mFileList = JSON.parseArray(JSON.parseObject(body).getString("data"), FileInfo.class);
                    for ( FileInfo file:mFileList) {
                        file.setDownloadState(FileUtil.fileExists(file.getName())?2:0);
                    }
                    mAdapter.setNewInstance(mFileList);
                    mAdapter.notifyDataSetChanged();
                });
            }

            @Override
            public void onFileDownload(byte[] bytes) {

            }

            @Override
            public void onFailed(int code, String message) {
                runOnUiThread(() -> {
                    Toast.makeText(MainActivity.this, "message:" + message, Toast.LENGTH_SHORT).show();
                });
            }
        }, (currentBytes, totalBytes, done) -> Log.d("wuzhi","currentBytes : " +currentBytes + " totalBytes:" + totalBytes + "done:" +done));
    }


    private void downLoadFile(String fileName,int fileIndex){
        if (FileUtil.fileExists(fileName)){
            mFileList.get(fileIndex).setDownloadState(2);
            if (!FileUtil.openFileFolder(this,fileName)) {
                Toast.makeText(this,"文件无法打开",Toast.LENGTH_SHORT).show();
            }
            return;
        }
        OkHttpUtil.doHttpRequestDownload("http://10.28.162.74:8080/download?fileName=" + fileName, new HttpRequestCallBack() {
            @Override
            public void onSuccess(String body) {

            }

            @Override
            public void onFileDownload(byte[] bytes) {
                FileUtil.saveFileToSdcard(fileName,bytes);
            }

            @Override
            public void onFailed(int code, String message) {

            }
        }, (currentBytes, totalBytes, done) -> {
            mFileList.get(fileIndex).setdTotalSize(totalBytes);
            mFileList.get(fileIndex).setdCurrentSize(currentBytes);
            mFileList.get(fileIndex).setDownloadState(done?2:1);
            runOnUiThread(() -> mAdapter.notifyDataSetChanged());

        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==1 && permissions[0].equals(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        &&grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            FileUtil.createSaveFileFolder(this);
        }
    }

    @SuppressLint("NonConstantResourceId")
    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.bt_select_file:
                mUploadFile = null;
                FileUtil.openFileFolder(this);
                break;
            case R.id.bt_upload:
                OkHttpUtil.doHttpUpload(mUploadFile, new HttpRequestCallBack() {
                    @Override
                    public void onSuccess(String body) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this,"上传完成",Toast.LENGTH_SHORT).show());
                    }

                    @Override
                    public void onFileDownload(byte[] bytes) {

                    }

                    @Override
                    public void onFailed(int code, String message) {
                        runOnUiThread(() -> Toast.makeText(MainActivity.this,"上传失败",Toast.LENGTH_SHORT).show());
                    }
                }, (currentBytes, totalBytes, done) -> {
                    Log.d("wuzhi","..." + currentBytes +"..." + totalBytes + "..."+done);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!done){
                                progressDialog.setProgress((int) ((currentBytes*100)/totalBytes));
                                if (!progressDialog.isShowing())
                                progressDialog.show();
                            } else {
                                progressDialog.dismiss();
                            }
                        }
                    });
                });
                break;
            default:break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null)return;
            mUploadFile = new File(uri.getPath());
            Log.d("wuzhi","filepath " +uri.getPath() + "...." + mUploadFile.exists());
            if (uri.getScheme().equals(ContentResolver.SCHEME_FILE)) {
                mUploadFile = new File(uri.getPath());
            } else if (uri.getScheme().equals(ContentResolver.SCHEME_CONTENT)) {
                //把文件复制到沙盒目录
                ContentResolver contentResolver = this.getContentResolver();
                String displayName = FileUtil.getFileRealNameFromUri(this,uri);
                InputStream is = null;
                try {
                    is = contentResolver.openInputStream(uri);
                    File cache = new File(this.getCacheDir().getAbsolutePath(), displayName);
                    FileOutputStream fos = new FileOutputStream(cache);
                    byte[] b = new byte[1024];
                    while ((is.read(b)) != -1) {
                        fos.write(b);// 写入数据
                    }
                    mUploadFile = cache;
                    Log.d("wuzhi","filepath1 " +mUploadFile.getPath() + "...." + mUploadFile.exists());
                    fos.close();
                    is.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            mSelectFile.setText(mUploadFile.getName());
        }
    }
}