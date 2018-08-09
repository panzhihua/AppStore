package com.rongyan.appstore.application;

import android.app.ActivityManager;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.multidex.MultiDex;
import android.util.Log;

import com.rongyan.appstore.BuildConfig;
import com.rongyan.appstore.activity.NetworksActivity;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.CacheUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.SignCheckUtils;
import com.tencent.bugly.crashreport.CrashReport;

import java.util.List;

public class AppStoreApplication extends Application {

    private final static String TAG="AppStoreApplication";

    private ConnectivityManager mConManager;

    private boolean updateNetwork = false;//判断网络状态是否发生改变

    private DataBaseOpenHelper mDataBaseOpenHelper=null;

    public static boolean isFirst=false;

    public boolean isfrontDesk=false;//应用是否在前台

    public boolean isReturn=false;//是否需要跳转到断网提示页面

    public boolean isClose=false;//是否需要关闭断网提示页面

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                String action = intent.getAction();
                if (action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                    if (mConManager == null) {
                        mConManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
                    }
                    NetworkInfo wifiInfo = mConManager
                            .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                    NetworkInfo ethInfo = mConManager
                            .getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
                    if ((wifiInfo != null && wifiInfo.isConnected()) || (ethInfo != null && ethInfo.isConnected())) {
                        if(isfrontDesk) {
                            Intent closeIntent = new Intent("close.activity");
                            sendBroadcast(closeIntent);//发送广播关闭断网提示activity
                            isClose=false;
                        }else{
                            isClose=true;
                        }
                        ApplicationUtils.setmNetWorkEnable(true);
                        updateNetwork = false;
                        LogUtils.w(TAG, "network_connection");
                        ApplicationUtils.refreshView();//刷新页面
                    } else {
                        if (!updateNetwork) {//解决连续2次提示网络不通bug
                            updateNetwork=true;
                            if(isfrontDesk) {
                                Intent mIntent = new Intent(getApplicationContext(), NetworksActivity.class);
                                mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(mIntent);
                                isReturn=false;
                            }else{
                                isReturn=true;
                            }
                            ApplicationUtils.setmNetWorkEnable(false);
                            LogUtils.w(TAG, "network_disconnect");
                        }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        Log.w("pipa","DD");
        String processName = getProcessName(AppStoreApplication.this);
        if (processName!= null) {
            if (processName.equals("com.rongyan.appstore")) {
                initSignature();
                initBasis();
                initCache();
                initBugly();
                initSystem();
                initRegister();
            }
        }
    }

    public void onTerminate() {
        LogUtils.w(TAG, "onTerminate()");

        unregisterReceiver(mBroadcastReceiver);
        // 程序终止的时候执行
        super.onTerminate();
    }

    public void initBasis(){
        try {
            LogUtils.w(TAG,"Build.DISPLAY="+Build.DISPLAY);
            ApplicationUtils.setmNetWorkEnable(ApplicationUtils.isNetworkAvailable(getApplicationContext()));
            ApplicationUtils.setAppList(getApplicationContext());
            String[] strArray = Build.DISPLAY.split("_");
            LogUtils.w(TAG,strArray[1]);
            if(strArray[1].equals("KOUBEI")){
                ApplicationUtils.setmBROKER("KB");
            }else {
                ApplicationUtils.setmBROKER(strArray[1]);
            }
            ApplicationUtils.setmMODEL(strArray[2]);
            ApplicationUtils.setmVERSION(strArray[3]);
//            ApplicationUtils.setmBROKER("EWH");
//            ApplicationUtils.setmMODEL("P10C");
//            ApplicationUtils.setmVERSION("2.0.7");
            IntentFilter filter = new IntentFilter();
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);// 监听网络变化
            registerReceiver(mBroadcastReceiver, filter);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initSignature(){
        SignCheckUtils signCheckUtils = new SignCheckUtils(getApplicationContext(),"FB:05:5C:7E:EB:D0:79:A4:87:2E:D5:0D:AD:A9:77:4C:E2:0C:84:47");
        if(signCheckUtils.check()) {
            LogUtils.w(TAG, "签名正确");
        }else {
            LogUtils.w(TAG, "签名不正确");
        }
    }

    public void initSystem(){
        try {
            mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getApplicationContext());
            int open_boot = Settings.System.getInt(getContentResolver(),"open_boot", 1);
            LogUtils.w(TAG, "open_boot="+open_boot);
            List<AppInfo> appInfo = mDataBaseOpenHelper.GetAllApp();
            if (appInfo != null && !appInfo.isEmpty()) {
                for (AppInfo mAppInfo : appInfo) {
                    if (open_boot==1) {
                        if (mAppInfo.getInstalling() == 1) {
                            mAppInfo.setInstalling(0);
                            mDataBaseOpenHelper.UpdateAppInstalling(mAppInfo);
                        }
                    }
                    if (mAppInfo.getInstalling() == 2) {
                        mAppInfo.setInstalling(0);
                        mDataBaseOpenHelper.UpdateAppInstalling(mAppInfo);
                    }
                }
            }
            Settings.System.putInt(getContentResolver(),"open_boot", 0);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void initCache(){
        ApplicationUtils.setApps_count(CacheUtils.getInt(getApplicationContext(), Constants.APPS_COUNT));
        ApplicationUtils.setBanners_count(CacheUtils.getInt(getApplicationContext(), Constants.BANNERS_COUNT));
        ApplicationUtils.setBanner_location(CacheUtils.getString(getApplicationContext(), Constants.BANNER_LOCATION));
        ApplicationUtils.setPer_page(CacheUtils.getInt(getApplicationContext(), Constants.PER_PAGE));
        ApplicationUtils.setCache_expires(CacheUtils.getInt(getApplicationContext(), Constants.CACHE_EXPIRES));
    }


    public void initBugly(){
        //bugly
        if(!BuildConfig.DEBUG) {
            CrashReport.initCrashReport(getApplicationContext());
            CrashReport.setUserId(Build.SERIAL);
        }
    }

    public void initRegister(){
        ForegroundCallbacks.init(this);

        ForegroundCallbacks.get().addListener(new ForegroundCallbacks.Listener() {
            @Override
            public void onBecameForeground() {
                LogUtils.d(TAG,"当前程序切换到前台");
                isfrontDesk=true;
                if(isReturn){
                    Intent mIntent = new Intent(getApplicationContext(), NetworksActivity.class);
                    mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(mIntent);
                    isReturn=false;
                }

                if(isClose){
                    Intent closeIntent = new Intent("close.activity");
                    sendBroadcast(closeIntent);//发送广播关闭断网提示activity
                    isClose=false;
                }
            }

            @Override
            public void onBecameBackground() {
                LogUtils.d(TAG,"当前程序切换到后台");
                isfrontDesk=false;
            }
        });

    }

    private String getProcessName(Context context) {
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningApps = am.getRunningAppProcesses();
        if (runningApps == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo proInfo : runningApps) {
            if (proInfo.pid == android.os.Process.myPid()) {
                if (proInfo.processName != null) {
                    return proInfo.processName;
                }
            }
        }
        return null;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

}
