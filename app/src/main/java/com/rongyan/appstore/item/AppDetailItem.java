package com.rongyan.appstore.item;

/**
 * 应用详情
 */

public class AppDetailItem extends Result{

    private Data data;

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public static class Data {

        private App app;

        public App getApp() {
            return app;
        }

        public void setApp(App app) {
            this.app = app;
        }

        public static class App extends Apps{

            private String introduce; // 介绍

            private String my_rating;// 我的评分，如果没有就不返回

            private String[] screenshots;//app图片介绍

            private Ratings ratings;//各分段评分人数

            public String getIntroduce() {
                return introduce;
            }

            public void setIntroduce(String introduce) {
                this.introduce = introduce;
            }

            public String getMy_rating() {
                return my_rating;
            }

            public void setMy_rating(String my_rating) {
                this.my_rating = my_rating;
            }

            public String[] getScreenshots() {
                return screenshots;
            }

            public void setScreenshots(String[] screenshots) {
                this.screenshots = screenshots;
            }

            public Ratings getRatings() {
                return ratings;
            }

            public void setRatings(Ratings ratings) {
                this.ratings = ratings;
            }

            public static class Ratings{
                private int five;

                private int four;

                private int three;

                private int two;

                private int one;

                public int getFive() {
                    return five;
                }

                public void setFive(int five) {
                    this.five = five;
                }

                public int getFour() {
                    return four;
                }

                public void setFour(int four) {
                    this.four = four;
                }

                public int getThree() {
                    return three;
                }

                public void setThree(int three) {
                    this.three = three;
                }

                public int getTwo() {
                    return two;
                }

                public void setTwo(int two) {
                    this.two = two;
                }

                public int getOne() {
                    return one;
                }

                public void setOne(int one) {
                    this.one = one;
                }
            }


        }

    }
}
