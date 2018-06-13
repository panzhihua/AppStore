package com.rongyan.appstore.item;

/**
 * app分类类
 */

public class AppClassItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private Apps[] apps;

        private Paging paging;

        public Apps[] getApps() {
            return apps;
        }

        public void setApps(Apps[] apps) {
            this.apps = apps;
        }

        public Paging getPaging() {
            return paging;
        }

        public void setPaging(Paging paging) {
            this.paging = paging;
        }

        public static class Paging{

            private int limit_value;//每页应用数量

            private int total_pages;//总页数

            private int current_page;//当前页

            private int next_page;//下一页页码

            private int prev_page;//上一页页码

            private boolean is_first_page;//是否第一页

            private boolean is_last_page;//是否最后一页

            private boolean is_out_of_range;//页码是否超过范围

            public int getLimit_value() {
                return limit_value;
            }

            public void setLimit_value(int limit_value) {
                this.limit_value = limit_value;
            }

            public int getTotal_pages() {
                return total_pages;
            }

            public void setTotal_pages(int total_pages) {
                this.total_pages = total_pages;
            }

            public int getCurrent_page() {
                return current_page;
            }

            public void setCurrent_page(int current_page) {
                this.current_page = current_page;
            }

            public int getNext_page() {
                return next_page;
            }

            public void setNext_page(int next_page) {
                this.next_page = next_page;
            }

            public int getPrev_page() {
                return prev_page;
            }

            public void setPrev_page(int prev_page) {
                this.prev_page = prev_page;
            }

            public boolean isIs_first_page() {
                return is_first_page;
            }

            public void setIs_first_page(boolean is_first_page) {
                this.is_first_page = is_first_page;
            }

            public boolean isIs_last_page() {
                return is_last_page;
            }

            public void setIs_last_page(boolean is_last_page) {
                this.is_last_page = is_last_page;
            }

            public boolean isIs_out_of_range() {
                return is_out_of_range;
            }

            public void setIs_out_of_range(boolean is_out_of_range) {
                this.is_out_of_range = is_out_of_range;
            }
        }
    }
}
