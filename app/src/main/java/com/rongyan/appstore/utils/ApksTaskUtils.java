package com.rongyan.appstore.utils;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Looper;

import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.item.AppDetailItem;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.AppInfoItem;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.item.PushClientItem;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;


public class ApksTaskUtils implements HttpGetUtils.CallBack,HttpPostUtils.CallBack,OkHttpDownAPKUtils.progress{

    private final static String TAG="ApksTaskUtils";

    public final static int OPEN=0;//打开

    public final static int DOWN=1;//下载

    public final static int DOWNING=2;//下载中

    public final static int INSTALL=3;//安装

    public final static int INSTALLING=4;//安装中

    public final static int UPDATE=5;//更新

    public static final int type_detail=1,type_callback=2,type_download=3;

    public String UNINSTALL_APP="app_uninstall",INSTALL_APP="app_install";

    private Context mContext;

    private Apps mApp;

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private int num=0;//记录网络请求次数

    private String appName="";//apk名字

    private int type_temporary=1;

    private Timer mAppDetailTimer,mCallBackTimer;

    private HttpGetUtils mAppDetailUtils;

    private HttpPostUtils mCallBackUtils;

    private OkHttpDownAPKUtils mHttpDownAPKUtils=null;

    private DataBaseOpenHelper mDataBaseOpenHelper=null;

    private PushClientItem mPushClientItem;

    private AppInstallItem mAppInstallItem;

    private AppUninstallItem mAppUninstallItem;

    private List<AppInfoItem> appItemList;

    private boolean isBroadCast=false;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            try{
                String action = intent.getAction();
                if (action.equals("package.install.returncode")) {
                    if(mApp!=null&&intent.getExtras().getString("name")!=null&&intent.getExtras().getString("name").equals(mApp.getPackage_name())) {
                        LogUtils.w(TAG, intent.getExtras().getString("name")+"=="+intent.getExtras().getInt("code"));
                        if (intent.getExtras().getInt("code") == 1) {
                            mAppInstallItem.setInstalled_at(StringUtils.getSystemDate());
                            postCallBack();
                        } else {
                            mAppInstallItem.setFail_reason("code:"+intent.getExtras().getInt("code"));
                            postCallBack();
                        }
                    }
                }else if (action.equals("package.delete.returncode")) {
                    LogUtils.w(TAG,  intent.getExtras().getString("name")+"=="+intent.getExtras().getInt("code"));
                    if(intent.getExtras().getString("name").equals(mPushClientItem.getPackage_name())){
                        if (intent.getExtras().getInt("code") == 1) {
                            mAppUninstallItem.setUninstalled_at(StringUtils.getSystemDate());
                            postCallBack();
                        } else {
                            postCallBack();
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    public ApksTaskUtils(Context context,PushClientItem pushClientItem){
        this.mContext=context;
        this.mPushClientItem=pushClientItem;
        this.mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(mContext);
        initBroadCast();
        if(UNINSTALL_APP.equals(pushClientItem.getNotification_type())){
            mAppUninstallItem=new AppUninstallItem();
            mAppUninstallItem.setNotification_id(pushClientItem.getNotification_id());
            mAppUninstallItem.setReceived_at(StringUtils.getSystemDate());
            mAppUninstallItem.setPackage_name(pushClientItem.getPackage_name());
            appItemList=ApplicationUtils.getAppInfos();
            if (appItemList != null && !appItemList.isEmpty()) {
                for (AppInfoItem appItem: appItemList) {
                    if (appItem.getAppPackageName().equals(pushClientItem.getPackage_name())) {
                        mAppUninstallItem.setVersion_code(appItem.getVersionCode());
                        mAppUninstallItem.setVersion_name(appItem.getVersionName());
                        break;
                    }
                }
            }
            sendBroadcastDelete(pushClientItem.getPackage_name());
        }else if(INSTALL_APP.equals(pushClientItem.getNotification_type())){
            mAppInstallItem=new AppInstallItem();
            mAppInstallItem.setNotification_id(pushClientItem.getNotification_id());
            mAppInstallItem.setReceived_at(StringUtils.getSystemDate());
            startTimer(type_detail,pushClientItem.getApp_no(),null);
        }
    }

    /**
     * 注册广播
     */
    private void initBroadCast(){
        if(!isBroadCast){
            IntentFilter filter = new IntentFilter();
            filter.addAction("package.install.returncode");
            filter.addAction("package.delete.returncode");
            mContext.registerReceiver(mBroadcastReceiver, filter);
            isBroadCast=true;
        }
    }

    public void startTimer(int type,String app_no,String data) {
        if (ApplicationUtils.ismNetWorkEnable()){
            if(type_temporary!=type){
                num=0;
                type_temporary=type;
            }else{
                num++;
            }
            if(type==type_detail) {
                LogUtils.w(TAG,"app_no:"+app_no);
                if (mAppDetailTimer != null) {
                    mAppDetailTimer.cancel();
                }
                mAppDetailTimer = new Timer();
                mAppDetailTimer.schedule(new AppDetailTask(app_no), 0);
            }else if(type==type_callback){
                LogUtils.w(TAG,"data:"+data);
                if (mCallBackTimer != null) {
                    mCallBackTimer.cancel();
                }
                mCallBackTimer = new Timer();
                mCallBackTimer.schedule(new CallBackTask(data), 0);
            }
        }
    }

    class AppDetailTask extends TimerTask {

        private String mApp_no;

        AppDetailTask(String app_no){
            if(app_no!=null) {
                mApp_no = app_no;
            }
        }

        @Override
        public void run() {
            mAppDetailUtils = new HttpGetUtils(mContext,ApksTaskUtils.this, Constants.HTTP_APP_NO_URL+mApp_no+"?include_unlaunched=true", mHandler);
            mAppDetailUtils.start();
        }
    }

    class CallBackTask extends TimerTask {

        private String mData;

        CallBackTask(String data){
            this.mData=data;
        }
        @Override
        public void run() {
            if(UNINSTALL_APP.equals(mPushClientItem.getNotification_type())){
                mCallBackUtils = new HttpPostUtils(mContext, ApksTaskUtils.this,Constants.HTTP_UNCALLBACK_URL, mHandler,mData);
                mCallBackUtils.start();
            }else if(INSTALL_APP.equals(mPushClientItem.getNotification_type())){
                mCallBackUtils = new HttpPostUtils( mContext,ApksTaskUtils.this,Constants.HTTP_INCALLBACK_URL, mHandler,mData);
                mCallBackUtils.start();
            }
        }
    }

    public void downAPK(){
        if(ApplicationUtils.isDownLoad(mApp)) {//判断是否还有下载空间
            if (ApplicationUtils.ismNetWorkEnable()) {
                putProgress(0,DOWNING,mApp.getNo());
                updateDataBase(2);
                mHttpDownAPKUtils = new OkHttpDownAPKUtils(mContext,ApksTaskUtils.this, mApp, appName,DOWNING);
                mHttpDownAPKUtils.download();
            }
        }else{
            mAppInstallItem.setFail_reason("no_space");
            postCallBack();
        }
    }

    /**
     * 发送广播
     */
    public void sendBroadcast(int state,int num,String app_no){
        if(mApp!=null) {
            Intent intent = new Intent();
            intent.setAction("action.update.appview");
            intent.putExtra("state", state);
            intent.putExtra("num", num);
            intent.putExtra("app_no", app_no);
            mContext.sendBroadcast(intent);
        }
    }

    /**
     * 发送广播(安装中)
     */
    public void sendBroadcastInstall(int state,int num,String app_no){
        //先修改显示文字
        sendBroadcast(state, num, app_no);
        //取消安装限制
        Intent install_intent = new Intent();
        install_intent.setAction("android.apps.write.list");
        install_intent.putExtra("keys", mApp.getPackage_name());
        mContext.sendBroadcast(install_intent);
        //然后静默安装
        Intent intent = new Intent();
        intent.setAction("action.install.apk");
        intent.putExtra("path", OkHttpDownAPKUtils.downloadPath + appName);
        intent.putExtra("package_name", mApp.getPackage_name());
        mContext.sendBroadcast(intent);
        //最后修改数据库状态
        updateDataBase(1);
    }

    /**
     *  修改数据库状态
     */
    public void updateDataBase(int installing){
        AppInfo appInfo=mDataBaseOpenHelper.QueryApp(mApp.getNo());
        if(appInfo==null){
            appInfo=new AppInfo();
            appInfo.setInstalled_times(mApp.getInstalled_times());
            if(mApp.getRatings_count()>0) {
                appInfo.setRatings(mApp.getRatings_sum() / mApp.getRatings_count());
            }else{
                appInfo.setRatings(0);
            }
            appInfo.setInstalling(installing);
            mDataBaseOpenHelper.AddApp(appInfo);
        }else{
            appInfo.setInstalling(installing);
            mDataBaseOpenHelper.UpdateAppInstalling(appInfo);
        }
    }
    /**
     * 发送广播(卸载应用)
     */
    public void sendBroadcastDelete(String package_name){
        Intent intent = new Intent();
        intent.setAction("action.delete.apk");
        intent.putExtra("package_name", package_name);
        mContext.sendBroadcast(intent);
    }

    @Override
    public void putProgress(int progress,int state,String appNo) {
        try {
            if (progress ==-2) {//说明apk已经存在
                if (state == DOWNING) {
                    updateDataBase(0);
                    mAppInstallItem.setDownloaded_at(StringUtils.getSystemDate());
                    sendBroadcastInstall(INSTALLING, 0,appNo);//安装中
                }
            }else if(progress ==-1){//说明apk下载失败
                updateDataBase(0);
                sendBroadcast(DOWN, 0,appNo);//下载
                mAppInstallItem.setFail_reason("down_failure");
                postCallBack();
            }else {
                sendBroadcast(state, progress * 100,appNo);
                if (progress >= 100) {//apk下载完毕
                    mAppInstallItem.setDownloaded_at(StringUtils.getSystemDate());
                    Thread.sleep(1000);
                    if (state == DOWNING) {
                        File file = new File(OkHttpDownAPKUtils.downloadPath + appName);
                        if(file.exists()) {//判断apk是否已经存在,可能在下载过程中把apk删除
                            sendBroadcastInstall(INSTALLING, 0,appNo);//安装中
                        }else{
                            updateDataBase(0);
                            mAppInstallItem.setFail_reason("down_failure");
                            postCallBack();
                            sendBroadcast(DOWN, 0,appNo);//下载
                        }
                    }
                }
            }

        } catch (Exception e) {
            mAppInstallItem.setFail_reason(e.toString());
            postCallBack();
            e.printStackTrace();
        }
    }

    /**
     * 回调云箭推送信息
     */
    private void postCallBack(){
        try {
            if(mPushClientItem.getNotification_type().equals(INSTALL_APP)){
                HashMap<String, String> map = new HashMap<>();
                map.put("notification_id", String.valueOf(mAppInstallItem.getNotification_id()));
                if(mAppInstallItem.getReceived_at()!=null&&!mAppInstallItem.getReceived_at().equals("")) {
                    map.put("received_at", mAppInstallItem.getReceived_at());
                }
                if(mAppInstallItem.getPackage_name()!=null&&!mAppInstallItem.getPackage_name().equals("")) {
                    map.put("package_name", mAppInstallItem.getPackage_name());
                }
                if(mAppInstallItem.getVersion_code()!=0) {
                    map.put("version_code", String.valueOf(mAppInstallItem.getVersion_code()));
                }
                if(mAppInstallItem.getVersion_name()!=null&&!mAppInstallItem.getVersion_name().equals("")) {
                    map.put("version_name", mAppInstallItem.getVersion_name());
                }
                if(mAppInstallItem.getOld_version_code()!=0) {
                    map.put("old_version_code", String.valueOf(mAppInstallItem.getOld_version_code()));
                }
                if(mAppInstallItem.getOld_version_name()!=null&&!mAppInstallItem.getOld_version_name().equals("")) {
                    map.put("old_version_name", mAppInstallItem.getOld_version_name());
                }
                if(mAppInstallItem.getApk_no()!=null&&!mAppInstallItem.getApk_no().equals("")) {
                    map.put("apk_no", mAppInstallItem.getApk_no());
                }
                if(mAppInstallItem.getApp_no()!=null&&!mAppInstallItem.getApp_no().equals("")) {
                    map.put("app_no", mAppInstallItem.getApp_no());
                }
                if(mAppInstallItem.getDownloaded_at()!=null&&!mAppInstallItem.getDownloaded_at().equals("")) {
                    map.put("downloaded_at", mAppInstallItem.getDownloaded_at());
                }
                if(mAppInstallItem.getInstalled_at()!=null&&!mAppInstallItem.getInstalled_at().equals("")) {
                    map.put("installed_at", mAppInstallItem.getInstalled_at());
                }
                if(mAppInstallItem.getFail_reason()!=null&&!mAppInstallItem.getFail_reason().equals("")) {
                    map.put("fail_reason", mAppInstallItem.getFail_reason());
                }
                String mString = JsonUtils.beanToJson(map);
                startTimer(type_callback, null,mString);
            }else if(mPushClientItem.getNotification_type().equals(UNINSTALL_APP)){
                HashMap<String, String> map = new HashMap<>();
                map.put("notification_id", String.valueOf(mAppUninstallItem.getNotification_id()));
                if(mAppUninstallItem.getReceived_at()!=null&&!mAppUninstallItem.getReceived_at().equals("")) {
                    map.put("received_at", mAppUninstallItem.getReceived_at());
                }
                if(mAppUninstallItem.getUninstalled_at()!=null&&!mAppUninstallItem.getUninstalled_at().equals("")) {
                    map.put("uninstalled_at", mAppUninstallItem.getUninstalled_at());
                }
                if(mAppUninstallItem.getPackage_name()!=null&&!mAppUninstallItem.getPackage_name().equals("")) {
                    map.put("package_name", mAppUninstallItem.getPackage_name());
                }
                if(mAppUninstallItem.getVersion_code()!=0) {
                    map.put("version_code", String.valueOf(mAppUninstallItem.getVersion_code()));
                }
                if(mAppUninstallItem.getVersion_name()!=null&&!mAppUninstallItem.getVersion_name().equals("")) {
                    map.put("version_name", mAppUninstallItem.getVersion_name());
                }
                String mString = JsonUtils.beanToJson(map);
                startTimer(type_callback, null,mString);
            }
        }catch(Exception e){
            startTimer(type_callback, null,null);
            e.printStackTrace();
        }
    }

    @Override
    public void setResponseData(String value) {
        try {
            LogUtils.w(TAG, "value:"+value);
            num=0;
            AppDetailItem item = (AppDetailItem) JsonUtils
                    .jsonToBean(value, AppDetailItem.class);
            if (item != null && item.isSuccess()) {
                if (item.getData() != null) {
                    mApp=item.getData().getApp();
                    if(mApp!=null){
                        mAppInstallItem.setApp_no(mApp.getNo());//获取详情成功
                        mAppInstallItem.setApk_no(mApp.getApk_no());
                        mAppInstallItem.setVersion_code(mApp.getVersion_code());
                        mAppInstallItem.setVersion_name(mApp.getVersion_name());
                        String[] sourceStrArray = mApp.getPackage_url().split("/");
                        appName=sourceStrArray[sourceStrArray.length-1];
                        appItemList=ApplicationUtils.getAppInfos();
                        boolean install=false;//默认App没有下载没有安装
                        if (appItemList != null && !appItemList.isEmpty()) {//先判断app是否已安装
                            for (AppInfoItem appItem : appItemList) {
                                if (appItem.getAppPackageName().equals(mApp.getPackage_name())) {
                                    mAppInstallItem.setOld_version_code(appItem.getVersionCode());
                                    mAppInstallItem.setOld_version_name(appItem.getVersionName());
                                    if(mApp.getVersion_code()<=appItem.getVersionCode()) {//判断版本号
                                        //不用操作
                                        mAppInstallItem.setDownloaded_at(StringUtils.getSystemDate());//下载成功的时间
                                        mAppInstallItem.setInstalled_at(StringUtils.getSystemDate());//安装成功的时间
                                        postCallBack();
                                        return;
                                    }else{//如果需要更新，先判断更新包是否已下载
                                        File file = new File(OkHttpDownAPKUtils.downloadPath + appName);
                                        if(file.exists()) {//判断apk是否已经存在
                                            if(file.length()<mApp.getPackage_size()) {//判断apk是否下载完成
                                                break;
                                            }else{
                                                mAppInstallItem.setDownloaded_at(StringUtils.getSystemDate());//下载成功的时间
                                                sendBroadcastInstall(INSTALLING, 0,mApp.getNo());//安装中
                                                return;
                                            }
                                        }
                                    }
                                    break;
                                }
                            }
                        }
                        if(!install){
                            downAPK();//下载apk
                        }
                    }else{
                        mAppInstallItem.setFail_reason("App=null");
                        postCallBack();
                    }
                }else{
                    mAppInstallItem.setFail_reason(item.getMessage());
                    postCallBack();
                }
            }else{
                mAppInstallItem.setFail_reason(item.getMessage());
                postCallBack();
            }
        }catch(Exception e){
            e.printStackTrace();
            mAppInstallItem.setFail_reason(e.toString());
            postCallBack();
        }
    }

    @Override
    public void setFailedResponse(String value) {
        num=0;
        mAppInstallItem.setFail_reason(value);
        postCallBack();
    }

    @Override
    public void setTimeoutResponse(String value) {
        if(num<3){
            startTimer(type_temporary,mApp.getNo(),null);
        }else{
            num=0;
            mAppInstallItem.setFail_reason(value);
            postCallBack();
        }
    }

    @Override
    public void setPostResponseData(String value) {
        LogUtils.w(TAG,value);
        num=0;
        if(isBroadCast) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            isBroadCast = false;
        }
    }

    @Override
    public void setPostFailedResponse(String value) {
        LogUtils.w(TAG,value);
        num=0;
        if(isBroadCast) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            isBroadCast = false;
        }
    }

    @Override
    public void setPostTimeoutResponse(String value) {
        LogUtils.w(TAG,value);
        if(num<3){
            postCallBack();
        }else{
            num=0;
            if(isBroadCast) {
                mContext.unregisterReceiver(mBroadcastReceiver);
                isBroadCast = false;
            }
        }
    }
}
