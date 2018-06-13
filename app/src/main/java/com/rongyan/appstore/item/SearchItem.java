package com.rongyan.appstore.item;

import java.util.List;

/**
 * 搜索结果返回类
 */

public class SearchItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private List<Apps> apps;

        public List<Apps> getApps() {
            return apps;
        }

        public void setApps(List<Apps> apps) {
            this.apps = apps;
        }
    }
}
