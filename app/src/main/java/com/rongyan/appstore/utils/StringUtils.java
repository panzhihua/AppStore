package com.rongyan.appstore.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

/**
 * 字符串工具类
 */

public class StringUtils {

    /**
     * 转换app文件大小
     */
    public static String formetFileSize(long fileS) {
        DecimalFormat df = new DecimalFormat("#.##");
        String fileSizeString = "";
        if (fileS < 1024) {
            fileSizeString = df.format((double) fileS) + "B";
        } else if (fileS < 1048576) {
            fileSizeString = df.format((double) fileS / 1024) + "K";
        } else if (fileS < 1073741824) {
            fileSizeString = df.format((double) fileS / 1048576) + "M";
        } else {
            fileSizeString = df.format((double) fileS / 1073741824) + "G";
        }
        return fileSizeString;
    }

    public static String getSystemDate(){
        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss Z", Locale.ENGLISH);
        String date = sDateFormat.format(new java.util.Date());
        return date;
    }

    public static int getSystemTime() {
        int unixTimestamp = (int)(System.currentTimeMillis() / 1000);
        return unixTimestamp;
    }

    public static boolean compareVersion(Context context,String code) {
        try{
            PackageManager manager = context.getPackageManager();
            PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
            int replace1 = Integer.parseInt(code.replace(".", "0"));
            int replace2 = Integer.parseInt(info.versionName.replace(".", "0"));
            if(replace1-replace2>0){
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return false;
    }
}
