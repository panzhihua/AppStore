package com.rongyan.appstore.item;

/**
 * 应用商城首页 banner 列表类
 */

public class BannersItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        private Banners[] banners;

        public Banners[] getBanners() {
            return banners;
        }

        public void setBanners(Banners[] banners) {
            this.banners = banners;
        }

        public static class Banners{

            private int id;

            private String title;

            private String picture_url;

            private int behavior; // 0: 跳转到指定 Url. 1: 跳转到指定 App.

            private String url;

            private App app;

            public static class App{

                private String no;

                private String package_name;

                public String getNo() {
                    return no;
                }

                public void setNo(String no) {
                    this.no = no;
                }

                public String getPackage_name() {
                    return package_name;
                }

                public void setPackage_name(String package_name) {
                    this.package_name = package_name;
                }
            }

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getTitle() {
                return title;
            }

            public void setTitle(String title) {
                this.title = title;
            }

            public String getPicture_url() {
                return picture_url;
            }

            public void setPicture_url(String picture_url) {
                this.picture_url = picture_url;
            }

            public int getBehavior() {
                return behavior;
            }

            public void setBehavior(int behavior) {
                this.behavior = behavior;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public App getApp() {
                return app;
            }

            public void setApp(App app) {
                this.app = app;
            }
        }
    }
}
