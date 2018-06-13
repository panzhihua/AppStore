package com.rongyan.appstore.application;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Handler;

import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.AppInfoItem;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.item.InstallItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.HttpDownAPKUtils;
import com.rongyan.appstore.utils.HttpPostUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.StringUtils;
import com.rongyan.appstore.widget.AppView;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 系统广播类
 */

public class AppInstallReceiver extends BroadcastReceiver implements HttpPostUtils.CallBack{

    private final static String TAG="AppInstallReceiver";

    private DataBaseOpenHelper mDataBaseOpenHelper=null;

    private Context mContext;

    private Handler mHandler = new Handler();

    private Timer mInstallTimer;

    private HttpPostUtils mInstallUtils;

    private int num=0;//记录网络请求次数

    private Apps mApp;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        mContext=context;
        if(mDataBaseOpenHelper==null) {
            mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(mContext);
        }
        if(ApplicationUtils.getAppMap().size()>0) {
            mApp=ApplicationUtils.getAppMap().get(intent.getData().toString());
        }
        if(Intent.ACTION_PACKAGE_ADDED.equals(action)){//一个新应用包已经安装在设备上，数据包括包名
            LogUtils.w(TAG,"ADDED");
            sendBroadcast(AppView.OPEN,0);
            putInstall();
            ApplicationUtils.setAppList(context);
            ApplicationUtils.setIsCheck_Update(true);
        }else if(Intent.ACTION_PACKAGE_REMOVED.equals(action)){//一个已存在的应用程序包已经从设备上移除，包括包名
            LogUtils.w(TAG,"REMOVED");
            sendBroadcast(AppView.DOWN,0);
            ApplicationUtils.setAppList(context);
            ApplicationUtils.setIsCheck_Update(true);
            ApplicationUtils.setIsInstall(true);
        }else if(Intent.ACTION_PACKAGE_REPLACED.equals(action)){//一个新版本的应用安装到设备，替换之前已经存在的版本
            LogUtils.w(TAG,"REPLACED");
        }
    }

    public void sendBroadcast(int state,int num){
        if(mApp!=null) {
            Intent intent = new Intent();
            intent.setAction("action.update.appview");
            intent.putExtra("state", state);
            intent.putExtra("num", num);
            intent.putExtra("app_no", mApp.getNo());
            mContext.sendBroadcast(intent);
        }
    }

    private void startTimer(String data) {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            if (mInstallTimer != null) {
                mInstallTimer.cancel();
            }
            mInstallTimer = new Timer();
            mInstallTimer.schedule(new InstallTask(data), 0);
        }else{

        }
    }

    class InstallTask extends TimerTask {

        private String mData;

        InstallTask(String data){
            this.mData=data;
        }
        @Override
        public void run() {
            mInstallUtils = new HttpPostUtils(mContext,AppInstallReceiver.this, String.format(Constants.HTTP_INSTALL_URL,mApp.getNo()), mHandler,mData);
            mInstallUtils.start();
        }
    }

    @Override
    public void setPostResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            num=0;
            InstallItem item = (InstallItem) JsonUtils
                    .jsonToBean(value, InstallItem.class);
            if (item != null && item.isSuccess()) {
                AppInfo appInfo=new AppInfo();
                appInfo.setApp_no(mApp.getNo());
                if(mDataBaseOpenHelper.QueryBeingApp(mApp.getNo()) ){//判断数据库中是否已存在
                    appInfo.setInstalled_times(""+item.getData().getInstalled_times());
                    mDataBaseOpenHelper.UpdateAppTnstalledTimes(appInfo);//更新
                }else{
                    appInfo.setRatings(0);
                    appInfo.setInstalled_times(""+item.getData().getInstalled_times());
                    appInfo.setInstalling(0);
                    mDataBaseOpenHelper.AddApp(appInfo);//添加
                }
                Intent intent = new Intent();
                intent.setAction("action.update.installs");
                intent.putExtra("installs", item.getData().getInstalled_times());
                intent.putExtra("app_no", mApp.getNo());
                mContext.sendBroadcast(intent);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setPostFailedResponse(String value) {
        LogUtils.w(TAG,value);
        num=0;
    }

    @Override
    public void setPostTimeoutResponse(String value) {
        LogUtils.w(TAG,value);
        if(num<3){
            putInstall();
        }else{
            num=0;
        }
    }

    /**
     * 安装(更新)回调
     */
    private void putInstall(){
        try {
            if(mApp!=null) {
                HashMap<String, String> map = new HashMap<>();
                map.put("apk_no", mApp.getApk_no());//apk序列号
                map.put("package_name", mApp.getPackage_name());//软件包名称
                map.put("version_code", String.valueOf(mApp.getVersion_code()));//版本号
                map.put("version_name", mApp.getVersion_name());//版本名称
                map.put("installed_at", StringUtils.getSystemDate());//安装完成时间
                if (ApplicationUtils.getAppInfos() != null && !ApplicationUtils.getAppInfos().isEmpty()) {
                    for (AppInfoItem mAppInfoItem : ApplicationUtils.getAppInfos()) {
                        if (mAppInfoItem.getAppPackageName().equals(mApp.getPackage_name())) {
                            map.put("old_version_code", String.valueOf(mAppInfoItem.getVersionCode()));//机器上已经安装的版本号
                            map.put("old_version_name", mAppInfoItem.getVersionName());//机器上已经安装的版本名称
                        }
                    }
                }
                HashMap<String, HashMap<String, String>> appMap = new HashMap<>();
                appMap.put("install_log", map);
                String mString = JsonUtils.beanToJson(appMap);
                startTimer(mString);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 安装完毕删除下载包
     */
    private void deleteFile(Apps app){
        try {
            if(app!=null) {
                String[] sourceStrArray = app.getPackage_url().split("/");
                String appName = sourceStrArray[sourceStrArray.length - 1];
                File file = new File(HttpDownAPKUtils.downloadPath + appName);
                if (file.exists()) {
                    if(!file.delete()){//删除失败再次删除
                        file.delete();
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     *  修改数据库状态
     */
    public void updateDataBase(){
        if(mApp!=null) {
            AppInfo appInfo = mDataBaseOpenHelper.QueryApp(mApp.getNo());
            if (appInfo == null) {
                appInfo = new AppInfo();
                appInfo.setInstalled_times(mApp.getInstalled_times());
                if (mApp.getRatings_count() > 0) {
                    appInfo.setRatings(mApp.getRatings_sum() / mApp.getRatings_count());
                } else {
                    appInfo.setRatings(0);
                }
                appInfo.setInstalling(0);
                mDataBaseOpenHelper.AddApp(appInfo);
            } else {
                appInfo.setInstalling(0);
                mDataBaseOpenHelper.UpdateAppInstalling(appInfo);
            }
        }
    }
}
