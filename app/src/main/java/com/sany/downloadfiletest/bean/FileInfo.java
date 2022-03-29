package com.sany.downloadfiletest.bean;

/**
 * @ClassName: FileInfo
 * @Description:
 * @Author: wuzhi.peng
 * @Date: 2022/3/25
 */
public class FileInfo {
    private String name;
    private String path;
    private long size;
    private String type;
    //0 未下载，1下载中，2下载完成
    private int downloadState = 0 ;
    private long dTotalSize;
    private long dCurrentSize ;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getDownloadState() {
        return downloadState;
    }

    public void setDownloadState(int downloadState) {
        this.downloadState = downloadState;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public long getdTotalSize() {
        return dTotalSize;
    }

    public void setdTotalSize(long dTotalSize) {
        this.dTotalSize = dTotalSize;
    }

    public long getdCurrentSize() {
        return dCurrentSize;
    }

    public void setdCurrentSize(long dCurrentSize) {
        this.dCurrentSize = dCurrentSize;
    }
}
