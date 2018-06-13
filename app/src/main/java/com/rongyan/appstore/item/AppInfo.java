package com.rongyan.appstore.item;

/**
 * app安装次数和评分信息类
 */

public class AppInfo {

    private String app_no;

    private String installed_times;

    private int ratings;

    private int installing;

    public AppInfo(){}

    public AppInfo(String app_no,String installed_times,int ratings,int installing){
        this.app_no=app_no;
        this.installed_times=installed_times;
        this.ratings=ratings;
        this.installing=installing;
    }

    public String getApp_no() {
        return app_no;
    }

    public void setApp_no(String app_no) {
        this.app_no = app_no;
    }

    public String getInstalled_times() {
        return installed_times;
    }

    public void setInstalled_times(String installed_times) {
        this.installed_times = installed_times;
    }

    public int getRatings() {
        return ratings;
    }

    public void setRatings(int ratings) {
        this.ratings = ratings;
    }

    public int getInstalling() {
        return installing;
    }

    public void setInstalling(int installing) {
        this.installing = installing;
    }
}
