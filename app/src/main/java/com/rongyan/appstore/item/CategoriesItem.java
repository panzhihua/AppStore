package com.rongyan.appstore.item;

/**
 * 应用商城左侧类别类
 */

public class CategoriesItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        private Categories[] categories;

        public Categories[] getCategories() {
            return categories;
        }

        public void setCategories(Categories[] categories) {
            this.categories = categories;
        }

        public static class Categories {

            private int id;

            private String name;

            private String icon_url;

            public int getId() {
                return id;
            }

            public void setId(int id) {
                this.id = id;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public String getIcon_url() {
                return icon_url;
            }

            public void setIcon_url(String icon_url) {
                this.icon_url = icon_url;
            }
        }

    }
}
