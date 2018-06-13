package com.rongyan.appstore.utils;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import com.rongyan.appstore.item.AppInfoItem;

import java.util.ArrayList;
import java.util.List;

/**
 * 扫描本地安装的应用,工具类
 */

public class ApkToolUtils {

    private static  String TAG = "ApkToolUtils";

    public static List<AppInfoItem> scanLocalInstallAppList(PackageManager packageManager) {
        List<AppInfoItem> myAppInfos = new ArrayList<AppInfoItem>();
        try {
            List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                //过滤掉系统app
//            if ((ApplicationInfo.FLAG_SYSTEM & packageInfo.applicationInfo.flags) != 0) {
//                continue;
//            }
                AppInfoItem myAppInfo = new AppInfoItem();
                myAppInfo.setAppName(packageManager.getApplicationLabel(packageInfo.applicationInfo).toString());
                myAppInfo.setAppPackageName(packageInfo.packageName);
                if (packageInfo.applicationInfo.loadIcon(packageManager) == null) {
                    continue;
                }
                myAppInfo.setVersionCode(packageInfo.versionCode);
                myAppInfo.setVersionName(packageInfo.versionName);
                myAppInfo.setImage(packageInfo.applicationInfo.loadIcon(packageManager));
                myAppInfos.add(myAppInfo);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return myAppInfos;
    }
}
