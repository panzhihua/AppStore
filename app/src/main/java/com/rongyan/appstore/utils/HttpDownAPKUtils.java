package com.rongyan.appstore.utils;
import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.rongyan.appstore.item.Apps;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;

import com.rongyan.appstore.R;;

/**
 * 下载类
 */

public class HttpDownAPKUtils extends Thread{

    // 下载文件 存放目的地
    public final static String downloadPath = Environment.getExternalStorageDirectory()
            .getPath() + "/AppStore/download_apk/";

    private final static String TAG="HttpDownAPKUtils";

    private long contentLength = 0;//服务器返回的数据长度

    private long readSize = 0L;//已下载的总大小

    private String appname;//下载apk名字

    private int mState;//状态

    private String startTime="",endTime="";//开始下载时间,结束下载时间

    private progress mProgress;//定义一个接口变量

    private int num;

    private Context mContext;

    private Handler mHandler=new Handler();

    private Apps app;

    /**
     * 定义一个接口
     */
    public interface progress{
        void putProgress(int progress,int state,String appNo);//返回下载状态和进度
    }

    public HttpDownAPKUtils(Context context, progress mProgress, Apps app, String appname, int state) {
        this.mContext=context;
        this.mProgress=mProgress;
        this.appname=appname;
        this.mState=state;
        this.app=app;
    }

    @Override
    public void run() {
        File tmpFile = new File(downloadPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        final File file = new File(downloadPath + appname);
        try {
            if(file.exists()){//判断apk是否已经存在
                if(file.length()<app.getPackage_size()) {//判断apk是否下载完成
                    readSize=file.length();//未下载完成则继续下载
                    mProgress.putProgress((int) (readSize * 100 / app.getPackage_size()), mState,app.getNo());
                }else{
                    mProgress.putProgress(-2, mState, app.getNo());//下载完成就直接安装
                    return;
                }
            }
            URL url = new URL(app.getPackage_url());
            HttpURLConnection connOne=null;
            InputStream is = null;
            RandomAccessFile raf=null;
            try {
                connOne = (HttpURLConnection) url
                        .openConnection();
                connOne.setReadTimeout(5000);
                connOne.setConnectTimeout(5000);
                connOne.setRequestProperty("Range", "bytes="+readSize+"-"+(app.getPackage_size()-1));
                contentLength = app.getPackage_size();
                is = connOne.getInputStream();
                raf = new RandomAccessFile(file, "rwd");
                raf.seek(readSize);
                byte[] buf = new byte[8192];
                connOne.connect();
                double count = 0;
                if (connOne.getResponseCode() == 206) {
                    num=0;
                    startTime=StringUtils.getSystemDate();
                    while (count <= 100) {
                        if (is != null) {
                            int numRead = is.read(buf);
                            if (numRead <= 0) {
                                break;
                            } else {
                                raf.write(buf, 0, numRead);
                                num++;
                                readSize = readSize + numRead;//累加已下载的大小
                                if (num>1599||(int) (readSize * 100 / contentLength)==100){
                                    num=0;
                                    mProgress.putProgress((int) (readSize * 100 / contentLength), mState,app.getNo());
                                }
                            }
                        } else {
                            break;
                        }
                    }
                }
            } catch (final IOException e) {
                if(e instanceof SocketTimeoutException){
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showToast(mContext, mContext.getString(R.string.download_failed_again)+e.toString());
                        }
                    });
                }
                mProgress.putProgress(-1, mState,app.getNo());
                e.printStackTrace();
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (connOne != null) {
                    try {
                        connOne.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            mProgress.putProgress(-1, mState,app.getNo());
            e.printStackTrace();
        }
    }

    public long getReadSize() {
        return readSize;
    }

    public String getStartTime() {
        return startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}

