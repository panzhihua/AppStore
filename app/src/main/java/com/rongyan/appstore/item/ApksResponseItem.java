package com.rongyan.appstore.item;

/**
 * Created by panzhihua on 2018/3/21.
 */

public class ApksResponseItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data{

        BootConfig boot_config;

        public BootConfig getBoot_config() {
            return boot_config;
        }

        public void setBoot_config(BootConfig boot_config) {
            this.boot_config = boot_config;
        }
    }

    public static class BootConfig{

        private Apk latest_rongyan_appstore_apk;

        public Apk getLatest_rongyan_appstore_apk() {
            return latest_rongyan_appstore_apk;
        }

        public void setLatest_rongyan_appstore_apk(Apk latest_rongyan_appstore_apk) {
            this.latest_rongyan_appstore_apk = latest_rongyan_appstore_apk;
        }

        public static class Apk{

            private int id;

            private String version;

            private String apk_file_url;

            private String apk_type;

            private String apk_type_text;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getVersion() {
                return version;
            }

            public void setVersion(String version) {
                this.version = version;
            }

            public String getApk_file_url() {
                return apk_file_url;
            }

            public void setApk_file_url(String apk_file_url) {
                this.apk_file_url = apk_file_url;
            }

            public String getApk_type() {
                return apk_type;
            }

            public void setApk_type(String apk_type) {
                this.apk_type = apk_type;
            }

            public String getApk_type_text() {
                return apk_type_text;
            }

            public void setApk_type_text(String apk_type_text) {
                this.apk_type_text = apk_type_text;
            }

        }
    }
}
