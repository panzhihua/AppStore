package com.rongyan.appstore.item;

/**
 * 应用最新版本查询返回类
 */

public class LatestVersionsItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private Apps[] apps;

        public Apps[] getApps() {
            return apps;
        }

        public void setApps(Apps[] apps) {
            this.apps = apps;
        }
    }
}
