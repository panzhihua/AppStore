package com.rongyan.appstore.activity;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;

import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.LogUtils;

import com.rongyan.appstore.R;

/**
 * 断网提示页
 */

public class NetworksActivity extends AppCompatActivity {

    private final static String TAG="NetworksActivity";

    private ImageView activity_Network_Refresh_img;

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals("close.activity")) {
                LogUtils.w(TAG, "close NetworksActivity");
                finish();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);
        init();
    }

    private void init(){
        initView();
        initEvent();
        initData();
    }

    private void initView(){
        activity_Network_Refresh_img = (ImageView) findViewById(R.id.activity_network_refresh_img);
    }

    private void initEvent(){
        activity_Network_Refresh_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ApplicationUtils.isNetworkAvailable(NetworksActivity.this)){
                    finish();
                }
            }
        });
    }

    private void initData(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("close.activity");// 监听网络变化
        registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exit();//返回桌面
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    /**
     * 返回桌面
     */
    private void exit() {
        Intent backHome = new Intent(Intent.ACTION_MAIN);
        backHome.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        backHome.addCategory(Intent.CATEGORY_HOME);
        startActivity(backHome);
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }
}
