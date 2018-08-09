package com.rongyan.appstore.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.rongyan.appstore.item.ApksResponseItem;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.OkHttpDownLoadAPKUtils;
import com.rongyan.appstore.utils.StringUtils;

import java.util.Timer;
import java.util.TimerTask;

/**
 * 开机启动检测升级服务
 */

public class AppStoreService extends Service implements HttpGetUtils.CallBack{

    private final static String TAG="AppStoreService";

    private HttpGetUtils mLatestUtils;

    private Timer mLatestTimer;

    private Handler mHandler = new Handler();

    private ConnectivityManager mConManager;

    private boolean isSuccess=false;

    private int num=0;//请求次数

    private ComponentName defaultComponent;

    private ComponentName testComponent;

    private PackageManager packageManager;

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
                       if(!isSuccess){
                           num=0;
                           startTimer(0);
                       }
                    }
                }
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);// 监听网络变化
        registerReceiver(mBroadcastReceiver, filter);
//        defaultComponent = new ComponentName(getBaseContext(), "com.rongyan.appstore.activity.MainActivity");  //拿到默认的组件
//        //拿到我注册的别名test组件
//        testComponent = new ComponentName(getBaseContext(), "com.rongyan.appstore.activity.test");
//
//        packageManager = getApplicationContext().getPackageManager();
//        disableComponent(defaultComponent);
//        enableComponent(testComponent);
    }

    public void startTimer(int delay) {
        if(num<3) {
            num++;
            if (mLatestTimer != null) {
                mLatestTimer.cancel();
            }
            mLatestTimer = new Timer();
            mLatestTimer.schedule(new LatestTask(), delay);
        }
    }

    class LatestTask extends TimerTask {

        @Override
        public void run() {
            mLatestUtils = new HttpGetUtils(AppStoreService.this, AppStoreService.this, Constants.HTTP_APPSTORE_LATEST_STRING, mHandler);
            mLatestUtils.start();
        }
    }

    @Override
    public void setResponseData(String value) {
        try {
            LogUtils.w(TAG,value);
            ApksResponseItem item = (ApksResponseItem) JsonUtils
                    .jsonToBean(value, ApksResponseItem.class);
            if (item != null && item.isSuccess()) {
                if (item.getData() != null) {
                    if(StringUtils.compareVersion(AppStoreService.this, item.getData().getApk().getVersion())){
                        if(item.getData().getApk().getApk_file_url()!=null&&!item.getData().getApk().getApk_file_url().equals("")){
                            isSuccess=true;
                            new OkHttpDownLoadAPKUtils(AppStoreService.this, item.getData().getApk().getApk_file_url()).download();
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setFailedResponse(String value) {
        startTimer(0);
    }

    @Override
    public void setTimeoutResponse(String value) {
        startTimer(0);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mBroadcastReceiver);
    }

    /**
     * 启用组件
     *
     * @param componentName
     */
    private void enableComponent(ComponentName componentName) {
        int state = packageManager.getComponentEnabledSetting(componentName);
        if (state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
            //已经启用
            return;
        }
        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    /**
     * 禁用组件
     *
     * @param componentName
     */
    private void disableComponent(ComponentName componentName) {
        int state = packageManager.getComponentEnabledSetting(componentName);
        if (state == PackageManager.COMPONENT_ENABLED_STATE_DISABLED) {
            //已经禁用
            return;
        }
        packageManager.setComponentEnabledSetting(componentName,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }

}
