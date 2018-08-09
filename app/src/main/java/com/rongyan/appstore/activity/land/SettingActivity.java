package com.rongyan.appstore.activity.land;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rongyan.appstore.fragment.land.OrderFragment;
import com.rongyan.appstore.fragment.land.UpdateFragment;
import com.rongyan.appstore.utils.ApplicationUtils;

import com.rongyan.appstore.R;;

/**
 * 横屏设置页面
 */

public class SettingActivity extends AppCompatActivity {

    public static final int UPDATE = 0;//应用更新

    public static final int ORDER =1;//我的订单

    private UpdateFragment updateFragment=null;

    private OrderFragment orderFragment=null;

    private ImageView setting_Back_Img,setting_Refresh_Img;

    private Button setting_Update_Btn;

    private TextView setting_Update_Txt,setting_Order_Txt,setting_Edition_Txt,setting_Back_Txt;

    private FrameLayout setting_Update_Fly,setting_Orders_Fly;

    private View setting_Update_View,setting_Order_View;

    private int currentIndex=-1;//记录当前Fragment编号

    private Fragment currentFragment =null;//记录当前Fragment页面
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        init();
    }

    @Override
    public void onResume(){
        ApplicationUtils.setActivity(this);
        super.onResume();
    }

    public void init(){
        initView();//实例化界面控件
        initEvent();
        initData();
    }

    public void initView(){
        setting_Back_Img = (ImageView) findViewById(R.id.setting_back_img);//返回按钮
        setting_Back_Txt = (TextView) findViewById(R.id.setting_back_txt);//返回按钮
        setting_Update_Btn = (Button) findViewById(R.id.setting_update_btn);//全部更新按钮
        setting_Refresh_Img = (ImageView) findViewById(R.id.setting_refresh_img);//刷新按钮
        setting_Update_Fly = (FrameLayout) findViewById(R.id.setting_update_fly);//"应用更新"背景
        setting_Update_Txt = (TextView) findViewById(R.id.setting_update_txt);//"应用更新"文字
        setting_Update_View = findViewById(R.id.setting_update_view);//"应用更新"下划线
        setting_Orders_Fly = (FrameLayout) findViewById(R.id.setting_order_fly);//"我的订单"背景
        setting_Order_Txt = (TextView) findViewById(R.id.setting_order_txt);//"我的订单"文字
        setting_Order_View =  findViewById(R.id.setting_order_view);//"我的订单"下划线
        setting_Edition_Txt = (TextView) findViewById(R.id.setting_edition_txt);//版本号
    }

    public void initEvent(){
        setting_Back_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setting_Back_Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        setting_Update_Fly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNavBtnEvent(UPDATE);
            }
        });
        setting_Orders_Fly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNavBtnEvent(ORDER);
            }
        });
        setting_Update_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updateFragment!=null){
                    updateFragment.initEvent();
                }
            }
        });
        setting_Refresh_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(updateFragment!=null){
                    updateFragment.postCheck_Update();
                }
            }
        });
    }

    public void initData(){
        executeNavBtnEvent(UPDATE);
        setting_Edition_Txt.setText(getString(R.string.current_version)+ ApplicationUtils.getAppVersion(this));
    }

    /**
     * 按钮切换tab页面事件
     */
    public void executeNavBtnEvent(int index) {
        if(currentIndex==index){
            return ;
        }
        setCurrentNavLayout(index,currentIndex);
        switchModule(index);
        currentIndex=index;
    }

    /**
     * 修改按钮背景
     */
    private void setCurrentNavLayout(int index, int currentIndex) {
        if(index==UPDATE){
            setting_Update_Fly.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white_10));
            if(ApplicationUtils.getmBROKER().equals("KB")) {
                setting_Update_View.setBackground(getResources().getDrawable(R.drawable.line_short_orangle));
            }else{
                setting_Update_View.setBackground(getResources().getDrawable(R.drawable.line_short_red));
            }
            setting_Update_Txt.setTextAppearance(getApplicationContext(), R.style.red_28_10);
            setting_Update_Btn.setVisibility(View.VISIBLE);
            setting_Refresh_Img.setVisibility(View.VISIBLE);
        }else if(index==ORDER){
            setting_Orders_Fly.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white_10));
            if(ApplicationUtils.getmBROKER().equals("KB")) {
                setting_Order_View.setBackground(getResources().getDrawable(R.drawable.line_short_orangle));
            }else{
                setting_Order_View.setBackground(getResources().getDrawable(R.drawable.line_short_red));
            }
            setting_Order_Txt.setTextAppearance(getApplicationContext(), R.style.red_28_10);
        }

        if(currentIndex==UPDATE){
            setting_Update_Fly.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grayF2_10));
            setting_Update_View.setBackground(getResources().getDrawable(R.drawable.line_short_gray));
            setting_Update_Txt.setTextAppearance(getApplicationContext(), R.style.gray37_24_10);
            setting_Update_Btn.setVisibility(View.GONE);
            setting_Refresh_Img.setVisibility(View.GONE);
        }else if(currentIndex==ORDER){
            setting_Orders_Fly.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.grayF2_10));
            setting_Order_View.setBackground(getResources().getDrawable(R.drawable.line_short_gray));
            setting_Order_Txt.setTextAppearance(getApplicationContext(), R.style.gray37_24_10);
        }
    }

    /**
     * 切换fragment操作
     */
    private void switchModule(int index) {
        switch (index) {
            case UPDATE:
                if(updateFragment==null){
                    updateFragment = new UpdateFragment();
                }
                setRightFragment(updateFragment);
                break;
            case ORDER:
                if(orderFragment==null){
                    orderFragment = new OrderFragment();
                }
                setRightFragment(orderFragment);
                break;
            default:
                break;
        }
    }

    private void setRightFragment(Fragment fragment) {
        hideFragment(currentFragment);//隐藏当前fragment页
        currentFragment = fragment;
        try {
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();
            if (fragment.isHidden()) {
                ft.show(fragment);
            } else {
                if (!fragment.isAdded()) {
                    ft.add(R.id.setting_right_fly, fragment);
                } else {
                    ft.show(fragment);
                }
            }
            ft.commitAllowingStateLoss();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private void hideFragment(Fragment fragment) {
        if(fragment!=null) {
            try {
                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction transaction = fragmentManager.beginTransaction();
                transaction.hide(fragment);
                transaction.commitAllowingStateLoss();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }
}
