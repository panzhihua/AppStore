package com.rongyan.appstore.item;

/**
 * 安装接口返回类
 */

public class InstallItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private int installed_times;

        public int getInstalled_times() {
            return installed_times;
        }

        public void setInstalled_times(int installed_times) {
            this.installed_times = installed_times;
        }
    }
}
