package com.rongyan.appstore.utils;

/**
 * 网络请求地址类
 */

public class Constants {

    public final static String CACHE="rongyan";

    public final static String BANNER_LOCATION="banner_location";

    public final static String BANNERS_COUNT="banners_count";

    public final static String APPS_COUNT="apps_count";

    public final static String PER_PAGE="per_page";

    public final static String CACHE_EXPIRES="cache_expires";

    public final static String DEVICE_TOKEN="device_token";

    private final static String HTTP_IP = "http://staging-rongyan-device.huo365.cn/api/";
    //private final static String HTTP_IP = "https://device.huo365.cn//api/";

    //private final static String HTTP_IP1 = "http://huo365.cn/api/";
    private final static String HTTP_IP1 = "http://staging.huo365.cn/api/";

    public final static String HTTP_SETTINGS_URL = HTTP_IP + "v1/appstore/settings";// 返回应用市场的配置信息

    public final static String HTTP_CATEGORIES_URL = HTTP_IP + "v1/appstore/categories";// 返回应用商城左侧类别列表

    public final static String HTTP_BANNERS_URL = HTTP_IP + "v1/appstore/banners?location=";// 返回应用商城首页 banner 列表

    public final static String HTTP_NEWEST_URL = HTTP_IP + "v1/appstore/apps/newest?count=";// 返回应用商城右下「最新」应用列表

    public final static String HTTP_HOTEST_URL = HTTP_IP + "v1/appstore/apps/hotest?count=";// 返回应用商城右下「最热」应用列表

    public final static String HTTP_DOWNLOAD_URL = HTTP_IP +"v1/appstore/apps/%s/download_logs";//应用下载回调接口

    public final static String HTTP_INSTALL_URL = HTTP_IP + "v1/appstore/apps/%s/install_logs";// 应用安装（更新）回调接口

    public final static String HTTP_APPS_URL = HTTP_IP + "v1/appstore/apps?app_category_id=%s&page=%s&per_page=%s";// 返回指定分类下应用列表

    public final static String HTTP_APP_NO_URL = HTTP_IP + "v1/appstore/apps/";// 返回应用详情

    public final static String HTTP_CHECK_UPDATE_URL = HTTP_IP + "v1/appstore/apps/check_update";// 应用最新版本查询接口

    public final static String HTTP_RATINGS_URL = HTTP_IP + "v1/appstore/apps/%s/ratings";// 应用评分接口

    public final static String HTTP_SEARCH_URL = HTTP_IP + "v1/appstore/apps?q=%s&no_paging=true";//搜索接口

    public final static String HTTP_CALLBACK_URL = HTTP_IP + "v1/appstore/install_app_notifications/callback";//云箭推送回调接口

    public final static String HTTP_APPSTORE_LATEST_STRING = HTTP_IP1 + "v1/cash_register/boot_config";// 获取应用市场最新的 Apk

    public final static String HTTP_INCALLBACK_URL = HTTP_IP + "v1/notification/app_install/callback";//云箭推送安装回调接口

    public final static String HTTP_UNCALLBACK_URL = HTTP_IP + "v1/notification/app_uninstall/callback";//云箭推送卸载回调接口
}
