package com.rongyan.appstore.utils;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;

import com.rongyan.appstore.activity.land.SettingActivity;
import com.rongyan.appstore.activity.port.AppDetailActivity;
import com.rongyan.appstore.activity.port.AppListActivity;
import com.rongyan.appstore.activity.port.HomePageActivity;
import com.rongyan.appstore.activity.port.UpdateActivity;
import com.rongyan.appstore.application.AppStoreApplication;
import com.rongyan.appstore.fragment.land.AppsFragment;
import com.rongyan.appstore.fragment.land.RecommendFragment;
import com.rongyan.appstore.fragment.port.HomePageFragment;
import com.rongyan.appstore.fragment.port.SettingFragment;
import com.rongyan.appstore.item.AppInfoItem;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.item.CategoriesItem;
import com.rongyan.appstore.widget.AppView;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 主要变量类
 */

public class ApplicationUtils {

    private final static String TAG="ApplicationUtils";

    private static boolean mNetWorkEnable;

    private static String mUUID = "";// 机器唯一编码

    private static String mBROKER = "";//渠道商代码，如 二维火 -> EWH

    private static String mMODEL = "";//设备型号

    private static String mVERSION = "";//软件版本

    private static String banner_location="";//banner位置

    private static int banners_count=0;//banner数量

    private static int apps_count=0;//最新、最热app数量

    private static int per_page=0;//app类别每页显示数量

    private static int cache_expires=0;//过期时间

    private static boolean isInstall=true;//是否能安装

    private static List<AppInfoItem> appInfos=null;//所有已安装的APP

    private static boolean isCheck_Update=true;//是否需要重新查询应用最新版本

    private static Map<String,Apps> appMap=new HashMap<>();

    private static Map<String,String> appDetailMap=new HashMap<>();

    private static Handler mainHandler = new Handler(Looper.getMainLooper());

    private static Activity activity;

    public static boolean ismNetWorkEnable() {
        return mNetWorkEnable;
    }

    public static void setmNetWorkEnable(boolean mNetWorkEnable) {
        ApplicationUtils.mNetWorkEnable = mNetWorkEnable;
    }

    /**
     * 获取UUID
     */

    public static String getUUID() {
        if (mUUID == null || mUUID.equals("0")|| mUUID.equals("")) {
            mUUID = getProperty("ro.aliyun.clouduuid", "0");
        }
        return mUUID;
    }

    /**
     * 获取 属性值
     *
     * @param key
     *            属性key
     * @param defaultValue
     *            默认值
     * @return 如果获取不到 就返回默认值
     */

    @SuppressWarnings({ "finally", "unused" })
    private static String getProperty(final String key,
                                      final String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(c, key, defaultValue));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return value;
        }
    }

    public static void setmBROKER(String mBROKER) {
        ApplicationUtils.mBROKER = mBROKER;
    }

    public static void setmMODEL(String mMODEL) {
        ApplicationUtils.mMODEL = mMODEL;
    }

    public static void setmVERSION(String mVERSION) {
        ApplicationUtils.mVERSION = mVERSION;
    }

    public static String getmBROKER() {
        return mBROKER;
    }

    public static String getmMODEL() {
        return mMODEL;
    }

    public static String getmVERSION() {
        return mVERSION;
    }

    public static int getBanners_count() {
        return banners_count;
    }

    public static void setBanners_count(int banners_count) {
        ApplicationUtils.banners_count = banners_count;
    }

    public static int getApps_count() {
        return apps_count;
    }

    public static void setApps_count(int apps_count) {
        ApplicationUtils.apps_count = apps_count;
    }

    public static int getPer_page() {
        return per_page;
    }

    public static void setPer_page(int per_page) {
        ApplicationUtils.per_page = per_page;
    }

    public static int getCache_expires() {
        return cache_expires;
    }

    public static void setCache_expires(int cache_expires) {
        ApplicationUtils.cache_expires = cache_expires;
    }

    public static String getBanner_location() {
        return banner_location;
    }

    public static void setBanner_location(String banner_location) {
        ApplicationUtils.banner_location = banner_location;
    }

    public static boolean isIsCheck_Update() {
        return isCheck_Update;
    }

    public static void setIsCheck_Update(boolean isCheck_Update) {
        ApplicationUtils.isCheck_Update = isCheck_Update;
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        ApplicationUtils.activity = activity;
    }

    public static Map<String, Apps> getAppMap() {
        return appMap;
    }

    public static void setAppMap(String packageName,Apps apps) {
        ApplicationUtils.appMap.put(packageName,apps);
    }

    public static Map<String, String> getAppDetailMap() {
        return appDetailMap;
    }

    public static void setAppDetailMap(Map<String, String> appDetailMap) {
        ApplicationUtils.appDetailMap = appDetailMap;
    }

    public static boolean isIsInstall() {
        return isInstall;
    }

    public static void setIsInstall(boolean isInstall) {
        ApplicationUtils.isInstall = isInstall;
    }

    /**
     * 检测当前网络（WLAN、3G/2G）状态
     * @return true 表示网络可用
     */
    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (info != null && info.isConnected())
            {
                // 当前网络是连接的
                if (info.getState() == NetworkInfo.State.CONNECTED)
                {
                    // 当前所连接的网络可用
                    return true;
                }
            }
        }
        return false;
    }

    /** 获取App版本号 **/
    public static String getAppVersion(Context context){
        String localVersion = "";
        try {
            localVersion = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e1) {
            e1.printStackTrace();
        }
        return localVersion;
    }

    /** 获取所有已安装的APP **/
    public static void setAppList(final Context context){
        new Thread(){
            @Override
            public void run() {
                super.run();
                //扫描得到APP列表
                appInfos = ApkToolUtils.scanLocalInstallAppList(context.getPackageManager());
                mainHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (getActivity() instanceof com.rongyan.appstore.activity.port.HomePageActivity) {//竖屏首页
                            Fragment mFragment = ((com.rongyan.appstore.activity.port.HomePageActivity) getActivity()).getCurrentFragment();
                            if (mFragment instanceof HomePageFragment) {
                                mFragment.onResume();
                            }else if(mFragment instanceof SettingFragment){
                                mFragment.onResume();
                            }
                        }else if (getActivity() instanceof AppListActivity) {//竖屏分类app页
                            ((AppListActivity) getActivity()).onResume();
                        }else if (getActivity() instanceof UpdateActivity) {//竖屏应用更新页
                            ((UpdateActivity) getActivity()).onResume();
                        }else if (getActivity() instanceof AppDetailActivity) {//竖屏app详情页
                            ((AppDetailActivity) getActivity()).onResume();
                        }else if (getActivity() instanceof com.rongyan.appstore.activity.land.HomePageActivity) {//横屏首页
                            Fragment mFragment = ((com.rongyan.appstore.activity.land.HomePageActivity) getActivity()).getCurrentFragment();
                            if (mFragment instanceof RecommendFragment) {
                                mFragment.onResume();
                            } else if (mFragment instanceof AppsFragment) {
                                mFragment.onResume();
                            }
                        }else if (getActivity() instanceof SettingActivity) {//横屏应用更新页
                            Fragment mFragment = ((SettingActivity) getActivity()).getCurrentFragment();
                            if (mFragment instanceof SettingFragment) {
                                mFragment.onResume();
                            }
                        }
                    }
                });
            }
        }.start();
    }
    /** 返回所有已安装的APP **/
    public static List<AppInfoItem> getAppInfos() {
        return appInfos;
    }

    /** 设置按钮状态 **/
    public static void setAppState(AppView appview,Apps app){
        boolean install=false;//默认App没有下载没有安装
        if (ApplicationUtils.getAppInfos() != null && !ApplicationUtils.getAppInfos().isEmpty()) {//先判断app是否已安装
            for (AppInfoItem appInfoItem : ApplicationUtils.getAppInfos()) {
                if (appInfoItem.getAppPackageName().equals(app.getPackage_name())) {
                    if(app.getVersion_code()<=appInfoItem.getVersionCode()) {//判断版本号
                        appview.sendBroadcast(AppView.OPEN, 0);//打开
                        return;
                    }else{//如果需要更新，先判断更新包是否已下载
                        File file = new File(HttpDownAPKUtils.downloadPath + appview.getAppName());
                        if(file.exists()) {//判断apk是否已经存在
                            if(file.length()<app.getPackage_size()) {//判断apk是否下载完成
                                appview.sendBroadcast(AppView.UPDATE,  (int)(10000*file.length()/app.getPackage_size()));//更新
                                appview.setInstall_update(AppView.UPDATE);
                                return;
                            }else{
                                appview.sendBroadcast(AppView.INSTALL, 0);//安装
                                return;
                            }
                        }else{
                            appview.sendBroadcast(AppView.UPDATE, 0);//更新
                            appview.setInstall_update(AppView.UPDATE);
                            return;
                        }
                    }
                }
            }
        }
        if(!install){//再判断app是否已下载
            File file = new File(HttpDownAPKUtils.downloadPath + appview.getAppName());
            if(file.exists()){//判断apk是否已经存在
                if(file.length()<app.getPackage_size()) {//判断apk是否下载完成
                    appview.setInstall_update(AppView.DOWN);
                    appview.sendBroadcast(AppView.DOWN,  (int)(10000*file.length()/app.getPackage_size()));//下载
                    return;
                }else{
                    appview.sendBroadcast(AppView.INSTALL, 0);//安装
                    return;
                }
            }
        }
        if(!install){
            appview.setInstall_update(AppView.DOWN);
            appview.sendBroadcast(AppView.DOWN,0);//下载
        }
    }

    /** 判断应用是否已安装 **/
    public static boolean isInstall(Apps app){
        if (ApplicationUtils.getAppInfos() != null && !ApplicationUtils.getAppInfos().isEmpty()) {//先判断app是否已安装
            for (AppInfoItem appInfoItem : ApplicationUtils.getAppInfos()) {
                if (appInfoItem.getAppPackageName().equals(app.getPackage_name())) {
                    return true;
                }
            }
        }
        return false;
    }

    /** 断网进入应用，连接网络刷新页面 **/
    public static void refreshView(){
        if(!AppStoreApplication.isFirst){
            if(getActivity() instanceof com.rongyan.appstore.activity.port.HomePageActivity){//竖屏
                ((com.rongyan.appstore.activity.port.HomePageActivity)getActivity()).startTimer();
            }else if(getActivity() instanceof com.rongyan.appstore.activity.land.HomePageActivity){//横屏
                ((com.rongyan.appstore.activity.land.HomePageActivity)getActivity()).startTimer(1,null,null,1000);
                ((com.rongyan.appstore.activity.land.HomePageActivity)getActivity()).getClassFragment().startTimer(1000);
            }
            AppStoreApplication.isFirst=true;
        }
    }

    /**
     * 判断是否有下载空间
     */
    public static boolean isDownLoad(Apps app){
        if(FileUtils.getAvailableSize()-app.getPackage_size()>100*1024*1024){
            return true;
        }
        return false;
    }

}
