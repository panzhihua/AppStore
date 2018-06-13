package com.rongyan.appstore.item;

/**
 * 下载进度类
 */

public class DownInfo {

    private String fileName;//文件名

    private String url;//下载地址

    private int length;//文件大小

    private long finished;//下载已完成进度

    private boolean isStop = false;//是否暂停下载

    public DownInfo(String fileName,String url,int length,long finished,boolean isStop){
        this.fileName=fileName;
        this.url=url;
        this.length=length;
        this.finished=finished;
        this.isStop=isStop;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public long getFinished() {
        return finished;
    }

    public void setFinished(long finished) {
        this.finished = finished;
    }

    public boolean isStop() {
        return isStop;
    }

    public void setStop(boolean stop) {
        isStop = stop;
    }
}
