package com.rongyan.appstore.item;

/**
 * 最新最热列表类
 */

public class NewestHotestItem extends Result{

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
