package com.rongyan.appstore.activity;

public interface PermissionsResultListener {

    //成功
    void onSuccessful(int[] grantResults);

    //失败
    void onFailure();
}
