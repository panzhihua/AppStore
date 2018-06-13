package com.rongyan.appstore.item;

/**
 * 应用市场配置信息类
 */

public class SettingItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        private Settings settings;

        public Settings getSettings() {
            return settings;
        }

        public void setSettings(Settings settings) {
            this.settings = settings;
        }

        public static class Settings {

            private String homepage_banner_location;

            private int homepage_banners_count;

            private int homepage_applist_count;

            private int cache_expires;

            private int app_version_code;

            private String app_package_url;

            private int categorylist_per_page;

            public String getHomepage_banner_location() {
                return homepage_banner_location;
            }

            public void setHomepage_banner_location(String homepage_banner_location) {
                this.homepage_banner_location = homepage_banner_location;
            }

            public int getHomepage_banners_count() {
                return homepage_banners_count;
            }

            public void setHomepage_banners_count(int homepage_banners_count) {
                this.homepage_banners_count = homepage_banners_count;
            }

            public int getHomepage_applist_count() {
                return homepage_applist_count;
            }

            public void setHomepage_applist_count(int homepage_applist_count) {
                this.homepage_applist_count = homepage_applist_count;
            }

            public int getCache_expires() {
                return cache_expires;
            }

            public void setCache_expires(int cache_expires) {
                this.cache_expires = cache_expires;
            }

            public int getApp_version_code() {
                return app_version_code;
            }

            public void setApp_version_code(int app_version_code) {
                this.app_version_code = app_version_code;
            }

            public String getApp_package_url() {
                return app_package_url;
            }

            public void setApp_package_url(String app_package_url) {
                this.app_package_url = app_package_url;
            }

            public int getCategorylist_per_page() {
                return categorylist_per_page;
            }

            public void setCategorylist_per_page(int categorylist_per_page) {
                this.categorylist_per_page = categorylist_per_page;
            }
        }
    }
}
