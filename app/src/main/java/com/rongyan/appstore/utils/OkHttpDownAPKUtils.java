package com.rongyan.appstore.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.rongyan.appstore.item.Apps;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

import com.rongyan.appstore.R;;

/**
 * OkHttp下载类
 */

public class OkHttpDownAPKUtils {
    // 下载文件 存放目的地
    public final static String downloadPath = Environment.getExternalStorageDirectory()
            .getPath() + "/AppStore/download_apk/";

    private OkHttpClient okHttpClient = new OkHttpClient();

    private final static String TAG="OkHttpDownAPKUtils";

    private long contentLength = 0;//服务器返回的数据长度

    private long readSize = 0L;//已下载的总大小

    private String appname;//下载apk名字

    private int mState;//状态

    private String startTime="",endTime="";//开始下载时间,结束下载时间

    private progress mOnDownloadListener;//定义一个接口变量

    private Context mContext;

    private Apps app;

    private Handler mHandler=new Handler();

    private int num;

    public OkHttpDownAPKUtils(Context context, progress onDownloadListener, Apps app, String appname, int state) {
        this.mContext=context;
        this.appname=appname;
        this.mState=state;
        this.app=app;
        this.mOnDownloadListener=onDownloadListener;
    }

    public void download() {
        File tmpFile = new File(downloadPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        final File file = new File(downloadPath + appname);
        if(file.exists()){//判断apk是否已经存在
            if(file.length()<app.getPackage_size()) {//判断apk是否下载完成
                readSize=file.length();
                mOnDownloadListener.putProgress((int) (readSize * 100 / app.getPackage_size()), mState,app.getNo());
            }else{
                mOnDownloadListener.putProgress(-2,mState,app.getNo());
                return;
            }
        }
        Request request = new Request.Builder().addHeader("Range", "bytes="+readSize+"-"+(app.getPackage_size()-1)).url(app.getPackage_url()).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        ToastUtils.showToast(mContext, mContext.getString(R.string.download_failed_again));
                    }
                });
                mOnDownloadListener.putProgress(-1,mState,app.getNo());
            }

            @Override
            public void onResponse(Response response) throws IOException {
                InputStream is = null;
                RandomAccessFile raf=null;
                try {
                    byte[] buf = new byte[2048];
                    int len = 0;
                    contentLength = app.getPackage_size();
                    is = response.body().byteStream();
                    long total = response.body().contentLength();
                    raf = new RandomAccessFile(file, "rwd");
                    raf.seek(readSize);
                    startTime=StringUtils.getSystemDate();
                    num=0;
                    while ((len = is.read(buf)) != -1) {
                        raf.write(buf, 0, len);
                        readSize += len;
                        num++;
                        // 下载中
                        if(num>1599) {
                            num=0;
                            mOnDownloadListener.putProgress((int) (readSize * 100 / contentLength), mState, app.getNo());
                        }
                    }
                    // 下载完成
                    mOnDownloadListener.putProgress(100,mState,app.getNo());
                } catch (Exception e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.showToast(mContext, app.getName()+mContext.getString(R.string.download_failed_again));
                        }
                    });
                    mOnDownloadListener.putProgress(-1,mState,app.getNo());
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (raf != null)
                            raf.close();
                    } catch (IOException e) {
                        mOnDownloadListener.putProgress(-1,mState,app.getNo());
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    public interface progress {
        /**
         * 下载进度
         */
        void putProgress(int progress,int state,String appNo);
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
