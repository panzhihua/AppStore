package com.rongyan.appstore.activity.port;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageView;


import com.rongyan.appstore.R;;

/**
 * 我的订单activity(竖屏)
 */

public class OrderActivity extends AppCompatActivity {

    private ImageView activity_Order_Back_Img,activity_Ordert_Refresh_Img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        init();
    }

    private void init(){
        initView();//实例化界面控件
        initEvent();
        initData();
    }

    private void initView(){
        activity_Order_Back_Img = (ImageView) findViewById(R.id.activity_order_back_img);
        activity_Ordert_Refresh_Img = (ImageView) findViewById(R.id.activity_order_refresh_img);
    }

    private void initEvent(){
        activity_Order_Back_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void initData(){
    }
}
