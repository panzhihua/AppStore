package com.rongyan.appstore.utils;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;

import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadListener;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.rongyan.appstore.R;
import com.rongyan.appstore.item.Apps;

import java.io.File;

public class DownloadManager {

    // 下载文件 存放目的地
    public final static String downloadPath = Environment.getExternalStorageDirectory()
            .getPath() + "/AppStore/download_apk/";

    private final static String TAG="DownloadManager";

    private Context mContext;

    private Handler mHandler=new Handler();

    private long readSize = 0L;//已下载的总大小

    private String appname;//下载apk名字

    private int mState;//状态

    private String startTime="",endTime="";//开始下载时间,结束下载时间

    private HttpDownAPKUtils.progress mProgress;//定义一个接口变量

    private Apps app;

    private int downloadId;

    private int retryNum=0;

    public DownloadManager(Context context, HttpDownAPKUtils.progress mProgress, Apps app, String appname, int state) {
        this.mContext=context;
        this.mProgress=mProgress;
        this.appname=appname;
        this.mState=state;
        this.app=app;
        this.retryNum=0;
    }

    public void download(){
        File tmpFile = new File(downloadPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        final File file = new File(downloadPath + appname);
        if(file.exists()){//判断apk是否已经存在
            if(file.length()<app.getPackage_size()) {//判断apk是否下载完成
                readSize=file.length();//未下载完成则继续下载
                mProgress.putProgress((int) (readSize * 100 / app.getPackage_size()), mState,app.getNo());
            }else{
                mProgress.putProgress(-2, mState, app.getNo());//下载完成就直接安装
                return;
            }
        }
        downloadId=FileDownloader.getImpl().create(app.getPackage_url())
                .setPath(downloadPath + appname,false)
                .setCallbackProgressTimes(10)//设置整个下载过程中FileDownloadListener#progress最大回调次数
                .setAutoRetryTimes(2)//当请求或下载或写文件过程中存在错误时，自动重试次数，默认为0次
                .setSyncCallback(true)
                .setListener(new FileDownloadSampleListener() {

                    @Override
                    protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {//等待，已经进入下载队列
                        super.pending(task, soFarBytes, totalBytes);
                        LogUtils.w(TAG,"pending:"+app.getPackage_url());
                    }

                    @Override
                    protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {//下载进度回调
                        super.progress(task, soFarBytes, totalBytes);
                        readSize=soFarBytes;
                        mProgress.putProgress((int)(Long.valueOf(soFarBytes)* 100 / Long.valueOf(totalBytes)), mState,app.getNo());
                        LogUtils.w(TAG,"soFarBytes="+soFarBytes+",totalBytes="+totalBytes+",progress="+(int)(Long.valueOf(soFarBytes)* 100 / Long.valueOf(totalBytes))+",completed:"+ task.getSpeed());
                    }

                    @Override
                    protected void error(BaseDownloadTask task, final Throwable e) {//下载出现错误
                        super.error(task, e);
                        mProgress.putProgress(-1, mState,app.getNo());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast(mContext, mContext.getString(R.string.download_failed_again)+e.toString());
                            }
                        });
                        LogUtils.w(TAG,"error:"+appname+":"+e.toString());
                    }

                    @Override
                    protected void connected(BaseDownloadTask task, String etag, boolean isContinue, int soFarBytes, int totalBytes) {//已经连接上
                        super.connected(task, etag, isContinue, soFarBytes, totalBytes);
                        startTime=StringUtils.getSystemDate();
                        LogUtils.w(TAG,"connected");
                    }

                    @Override
                    protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {//暂停下载
                        super.paused(task, soFarBytes, totalBytes);
                        FileDownloader.getImpl().clear(downloadId,downloadPath + appname);
                        task.start();
                        LogUtils.w(TAG,"paused");
                    }

                    @Override
                    protected void completed(BaseDownloadTask task) {//完成整个下载过程
                        super.completed(task);
                        mProgress.putProgress(100, mState,app.getNo());
                        LogUtils.w(TAG,"completed:"+appname+":"+ task.getSpeed());
                    }

                    @Override
                    protected void warn(BaseDownloadTask task) {//在下载队列中(正在等待/正在下载)已经存在相同下载连接与相同存储路径的任务
                        super.warn(task);
                        FileDownloader.getImpl().clear(downloadId,downloadPath + appname);
                        mProgress.putProgress(-1, mState,app.getNo());
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                ToastUtils.showToast(mContext, mContext.getString(R.string.download_failed_again));
                            }
                        });
                        LogUtils.w(TAG,"warn"+appname);
                    }

                    @Override
                    protected void retry(BaseDownloadTask task,final Throwable ex,int retryingTimes, int soFarBytes)  {
                        super.warn(task);
                        retryNum++;
                        if(retryNum>4){
                            mProgress.putProgress(-1, mState,app.getNo());
                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    ToastUtils.showToast(mContext, mContext.getString(R.string.download_failed_again)+ex.toString());
                                }
                            });
                        }
                        LogUtils.w(TAG,"retry"+"{"+retryNum+"}"+appname+":"+ex.toString());
                    }
                }).start();
        LogUtils.w(TAG,"downloadId="+downloadId);

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
