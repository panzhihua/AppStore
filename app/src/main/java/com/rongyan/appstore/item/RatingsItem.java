package com.rongyan.appstore.item;

/**
 * 评分接口返回类
 */

public class RatingsItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {
        private int ratings_count;

        private int ratings_sum;

        public int getRatings_count() {
            return ratings_count;
        }

        public void setRatings_count(int ratings_count) {
            this.ratings_count = ratings_count;
        }

        public int getRatings_sum() {
            return ratings_sum;
        }

        public void setRatings_sum(int ratings_sum) {
            this.ratings_sum = ratings_sum;
        }
    }
}
