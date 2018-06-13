package com.rongyan.appstore.item;

import android.graphics.drawable.Drawable;

/**
 * App应用类
 */

public class AppInfoItem {

    private Drawable image;//app图标

    private String appName;//app名称

    private String appPackageName;//app包名

    private int versionCode;//app版本号

    private String versionName;//app版本名称

    public AppInfoItem() {

    }

    public Drawable getImage() {
        return image;
    }

    public void setImage(Drawable image) {
        this.image = image;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getAppPackageName() {
        return appPackageName;
    }

    public void setAppPackageName(String appPackageName) {
        this.appPackageName = appPackageName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public void setVersionCode(int versionCode) {
        this.versionCode = versionCode;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }
}
