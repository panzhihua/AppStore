package com.rongyan.appstore.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.igexin.sdk.PushManager;
import com.rongyan.appstore.service.AppStoreService;
import com.rongyan.appstore.utils.ApplicationUtils;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 判断Android当前的屏幕是横屏还是竖屏。横竖屏判断
        PushManager.getInstance().initialize(this.getApplicationContext(), com.rongyan.appstore.service.DemoPushService.class);
        PushManager.getInstance().registerPushIntentService(this.getApplicationContext(), com.rongyan.appstore.service.DemoIntentService.class);
        PushManager.getInstance().bindAlias(getApplicationContext(), ApplicationUtils.getSN(), "merchant");//个推别名
        if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            //竖屏
            startActivity(new Intent(MainActivity.this, com.rongyan.appstore.activity.port.HomePageActivity.class));
            finish();
        } else {
            //横屏
            startActivity(new Intent(MainActivity.this, com.rongyan.appstore.activity.land.HomePageActivity.class));
            finish();
        }

    }

}
