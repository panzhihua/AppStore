package com.rongyan.appstore.item;

/**
 * app应用类
 */

public class Apps {

    private String no; // 应用标识，唯一标识一个应用，类似 id

    private String apk_no;//apk序列号

    private String icon_url; // 图标地址

    private String name; // 应用名称

    private int ratings_count; // 评分次数

    private int ratings_sum; // 评分总数

    private String installed_times; // 安装次数

    private int version_code; // 版本 id, 在下载/安装回调接口中反传

    private String version_name; // 版本

    private String package_url; // 包地址

    private int package_size; // 包大小

    private String package_md5;//md5校验

    private String package_name; // 包名

    private String changelog;//更新说明

    public String getNo() {
        return no;
    }

    public void setNo(String no) {
        this.no = no;
    }

    public String getApk_no() {
        return apk_no;
    }

    public void setApk_no(String apk_no) {
        this.apk_no = apk_no;
    }

    public String getIcon_url() {
        return icon_url;
    }

    public void setIcon_url(String icon_url) {
        this.icon_url = icon_url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRatings_count() {
        return ratings_count;
    }

    public void setRatings_count(int ratings_count) {
        this.ratings_count = ratings_count;
    }

    public int getRatings_sum() {
        return ratings_sum;
    }

    public void setRatings_sum(int ratings_sum) {
        this.ratings_sum = ratings_sum;
    }

    public String getInstalled_times() {
        return installed_times;
    }

    public void setInstalled_times(String installed_times) {
        this.installed_times = installed_times;
    }

    public int getVersion_code() {
        return version_code;
    }

    public void setVersion_code(int version_code) {
        this.version_code = version_code;
    }

    public String getVersion_name() {
        return version_name;
    }

    public void setVersion_name(String version_name) {
        this.version_name = version_name;
    }

    public String getPackage_url() {
        return package_url;
    }

    public void setPackage_url(String package_url) {
        this.package_url = package_url;
    }

    public int getPackage_size() {
        return package_size;
    }

    public void setPackage_size(int package_size) {
        this.package_size = package_size;
    }

    public String getPackage_name() {
        return package_name;
    }

    public void setPackage_name(String package_name) {
        this.package_name = package_name;
    }

    public String getPackage_md5() {
        return package_md5;
    }

    public void setPackage_md5(String package_md5) {
        this.package_md5 = package_md5;
    }

    public String getChangelog() {
        return changelog;
    }

    public void setChangelog(String changelog) {
        this.changelog = changelog;
    }
}
