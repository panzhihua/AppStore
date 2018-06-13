package com.rongyan.appstore.activity.port;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.fragment.land.RecommendFragment;
import com.rongyan.appstore.fragment.port.ClassificationFragment;
import com.rongyan.appstore.fragment.port.HomePageFragment;
import com.rongyan.appstore.fragment.port.SettingFragment;
import com.rongyan.appstore.item.SettingItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.CacheUtils;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.PermissionUtils;
import com.rongyan.appstore.utils.StringUtils;
import com.rongyan.appstore.utils.ToastUtils;
import com.rongyan.appstore.widget.AppView;

import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;;

/**
 * 竖屏首页
 */

public class HomePageActivity extends FragmentActivity implements HttpGetUtils.CallBack,AppView.app,ActivityCompat.OnRequestPermissionsResultCallback{

    private final static String TAG="HomePageActivity";

    public static final int HOMEPAGE = 1;//首页

    public static final int CLASSIFICATION =2;//分类

    public static final int SETTING = 3;//设置

    public static final int type_setting=1;

    private SettingFragment settingFragment;

    private HomePageFragment homePageFragment;

    private ClassificationFragment classificationFragment;

    private FrameLayout activity_Homepage_Fly;

    private LinearLayout activity_Homepage_Home_Liy,activity_Homepage_Focus_Liy,activity_Homepage_Setting_Liy,activity_Homepage_Search_Liy;

    private ImageView activity_Homepage_Refresh_Img,activity_Homepage_Unhome_Img,activity_Homepage_Home_Img,activity_Homepage_Unfocus_Img,activity_Homepage_Focus_Img,activity_Homepage_Unsetting_Img,activity_Homepage_Setting_Img;

    private TextView activity_Homepage_Unhome_Txt,activity_Homepage_Home_Txt,activity_Homepage_Unfocus_Txt,activity_Homepage_Focus_Txt,activity_Homepage_Unsetting_Txt,activity_Homepage_Setting_Txt;

    private EditText activity_Homepage_Search_Edit;

    private int currentIndex;//记录当前Fragment编号

    private Fragment currentFragment =null;//记录当前Fragment页面

    private Timer mSettingTimer;

    private HttpGetUtils mSettingUtils;

    private Handler mHandler = new Handler();

    private int num=0;

    private String mSearch;

    private int last_refresh;//上一次刷新时间

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.app.permission")) {
                if(intent.getExtras().getInt("type")==PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE){
                    PermissionUtils.requestPermission(HomePageActivity.this, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE, mPermissionGrant);
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.w("pipa","BB");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        init();
    }

    @Override
    public void onResume(){
        super.onResume();
        ApplicationUtils.setActivity(this);
        getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //获取View可见区域的bottom
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                if(bottom!=0 && oldBottom!=0 && bottom - rect.bottom <= 0){
                    activity_Homepage_Fly.setVisibility(View.GONE);
                }else {
                    activity_Homepage_Fly.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    public void init(){
        initBroadCast();
        initView();//实例化界面控件
        initEvent();
        initData();
    }

    /**
     * 注册广播
     */
    private void initBroadCast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("action.app.permission");
        registerReceiver(mBroadcastReceiver, filter);
    }

    public void initView() {
        activity_Homepage_Refresh_Img = (ImageView) findViewById(R.id.activity_homepage_refresh_img);//刷新按钮
        activity_Homepage_Home_Liy = (LinearLayout) findViewById(R.id.activity_homepage_home_liy);//首页
        activity_Homepage_Unhome_Txt = (TextView) findViewById(R.id.activity_homepage_unhome_txt);//
        activity_Homepage_Unhome_Img = (ImageView) findViewById(R.id.activity_homepage_unhome_img);//
        activity_Homepage_Home_Txt = (TextView) findViewById(R.id.activity_homepage_home_txt);//
        activity_Homepage_Home_Img = (ImageView) findViewById(R.id.activity_homepage_home_img);//
        activity_Homepage_Focus_Liy = (LinearLayout) findViewById(R.id.activity_homepage_focus_liy);//分类
        activity_Homepage_Unfocus_Txt = (TextView) findViewById(R.id.activity_homepage_unfocus_txt);//
        activity_Homepage_Unfocus_Img = (ImageView) findViewById(R.id.activity_homepage_unfocus_img);//
        activity_Homepage_Focus_Txt = (TextView) findViewById(R.id.activity_homepage_focus_txt);//
        activity_Homepage_Focus_Img = (ImageView) findViewById(R.id.activity_homepage_focus_img);//
        activity_Homepage_Setting_Liy = (LinearLayout) findViewById(R.id.activity_homepage_setting_liy);//设置
        activity_Homepage_Unsetting_Txt = (TextView) findViewById(R.id.activity_homepage_unsetting_txt);//
        activity_Homepage_Unsetting_Img = (ImageView) findViewById(R.id.activity_homepage_unsetting_img);//
        activity_Homepage_Setting_Txt = (TextView) findViewById(R.id.activity_homepage_setting_txt);//
        activity_Homepage_Setting_Img = (ImageView) findViewById(R.id.activity_homepage_setting_img);//
        activity_Homepage_Search_Liy = (LinearLayout) findViewById(R.id.activity_homepage_search_liy);//
        activity_Homepage_Search_Edit = (EditText) findViewById(R.id.activity_homepage_search_edit);//
        activity_Homepage_Fly = (FrameLayout) findViewById(R.id.activity_homepage_Fly);
        activity_Homepage_Search_Edit.setFocusable(false);//隐藏焦点
    }

    public void initEvent() {
        activity_Homepage_Home_Liy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNavBtnEvent(HOMEPAGE);
            }
        });
        activity_Homepage_Focus_Liy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNavBtnEvent(CLASSIFICATION);
            }
        });
        activity_Homepage_Setting_Liy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                executeNavBtnEvent(SETTING);
            }
        });
        activity_Homepage_Refresh_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StringUtils.getSystemTime()-last_refresh>3) {
                    last_refresh = StringUtils.getSystemTime();
                    if (ApplicationUtils.getBanner_location() == null || ApplicationUtils.getBanner_location().equals("")) {
                        startTimer();
                    } else {
                        if (currentFragment != null) {
                            if (currentFragment instanceof HomePageFragment) {
                                ((HomePageFragment) currentFragment).startTimer(((HomePageFragment) currentFragment).getHotOrNew());
                                ((HomePageFragment) currentFragment).startTimer(HomePageFragment.type_banner);
                            } else if (currentFragment instanceof ClassificationFragment) {
                                ((ClassificationFragment) currentFragment).startTimer(0);
                            } else if (currentFragment instanceof SettingFragment) {
                                ((SettingFragment) currentFragment).postCheck_Update();
                            }
                        }
                    }
                }
            }
        });

        activity_Homepage_Search_Edit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入的内容变化的监听
                if(s.toString().equals("")){
                    activity_Homepage_Search_Liy.setVisibility(View.VISIBLE);
                }else{
                    activity_Homepage_Search_Liy.setVisibility(View.GONE);
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
                // 输入前的监听
            }

            @Override
            public void afterTextChanged(Editable s) {
                // 输入后的监听
            }
        });
        activity_Homepage_Search_Edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                activity_Homepage_Search_Edit.setFocusable(true);
                activity_Homepage_Search_Edit.setFocusableInTouchMode(true);
                return false;
            }
        });
    }

    public void initData() {
        executeNavBtnEvent(HOMEPAGE);
        startTimer();
    }

    /**
     * 底部按钮切换tab页面事件
     */
    public void executeNavBtnEvent(int index) {
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE, mPermissionGrant);
        PermissionUtils.requestPermission(this, PermissionUtils.CODE_ACCESS_FINE_LOCATION, mPermissionGrant);
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
    private void setCurrentNavLayout(int index,int currentIndex) {
        if(index==HOMEPAGE) {
            activity_Homepage_Unhome_Txt.setVisibility(View.GONE);
            activity_Homepage_Home_Txt.setVisibility(View.VISIBLE);
            activity_Homepage_Unhome_Img.setVisibility(View.GONE);
            activity_Homepage_Home_Img.setVisibility(View.VISIBLE);
        }else if(index==CLASSIFICATION){
            activity_Homepage_Unfocus_Txt.setVisibility(View.GONE);
            activity_Homepage_Focus_Txt.setVisibility(View.VISIBLE);
            activity_Homepage_Unfocus_Img.setVisibility(View.GONE);
            activity_Homepage_Focus_Img.setVisibility(View.VISIBLE);
        }else if(index==SETTING){
            activity_Homepage_Unsetting_Txt.setVisibility(View.GONE);
            activity_Homepage_Setting_Txt.setVisibility(View.VISIBLE);
            activity_Homepage_Unsetting_Img.setVisibility(View.GONE);
            activity_Homepage_Setting_Img.setVisibility(View.VISIBLE);
        }
        if(currentIndex==HOMEPAGE){
            activity_Homepage_Unhome_Txt.setVisibility(View.VISIBLE);
            activity_Homepage_Home_Txt.setVisibility(View.GONE);
            activity_Homepage_Unhome_Img.setVisibility(View.VISIBLE);
            activity_Homepage_Home_Img.setVisibility(View.GONE);
        }else if(currentIndex==CLASSIFICATION){
            activity_Homepage_Unfocus_Txt.setVisibility(View.VISIBLE);
            activity_Homepage_Focus_Txt.setVisibility(View.GONE);
            activity_Homepage_Unfocus_Img.setVisibility(View.VISIBLE);
            activity_Homepage_Focus_Img.setVisibility(View.GONE);
        }else if(currentIndex==SETTING){
            activity_Homepage_Unsetting_Txt.setVisibility(View.VISIBLE);
            activity_Homepage_Setting_Txt.setVisibility(View.GONE);
            activity_Homepage_Unsetting_Img.setVisibility(View.VISIBLE);
            activity_Homepage_Setting_Img.setVisibility(View.GONE);
        }
    }

    /**
     * 切换fragment操作
     */
    private void switchModule(int index) {
        switch (index) {
            case HOMEPAGE:
                if(homePageFragment==null){
                    homePageFragment = new HomePageFragment();
                }
                setFragment(homePageFragment);
                break;
            case CLASSIFICATION:
                if(classificationFragment==null){
                    classificationFragment = new ClassificationFragment();
                }
                setFragment(classificationFragment);
                break;
            case SETTING:
                if(settingFragment==null){
                    settingFragment = new SettingFragment();
                }
                setFragment(settingFragment);
                break;
            default:
                break;
        }
    }

    private void setFragment(Fragment fragment) {
        hideFragment(currentFragment);//隐藏当前fragment页
        currentFragment = fragment;
        try {
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();
            if (fragment.isHidden()) {
                ft.show(fragment);
            } else {
                if (!fragment.isAdded()) {
                    ft.add(R.id.activity_homepage_fly, fragment);
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

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            outState.remove(FRAGMENTS_TAG);// remove掉保存的Fragment
        }
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mBroadcastReceiver);
        super.onDestroy();
    }

    public void startTimer() {
        if (ApplicationUtils.ismNetWorkEnable()){
            if (mSettingTimer != null) {
                mSettingTimer.cancel();
            }
            mSettingTimer = new Timer();
            mSettingTimer.schedule(new SettingTask(), 0);
        }else{
            Intent mIntent = new Intent(HomePageActivity.this, com.rongyan.appstore.activity.NetworksActivity.class);
            startActivity(mIntent);
            ToastUtils.showToast(HomePageActivity.this,getString(R.string.network_failed_check_configuration));
        }
    }

    @Override
    public void getAppNo(String app_no) {
        Intent intent = new Intent(HomePageActivity.this, AppDetailActivity.class);
        intent.putExtra("app_no",app_no);
        startActivity(intent);
    }

    class SettingTask extends TimerTask {
        @Override
        public void run() {
            mSettingUtils = new HttpGetUtils(HomePageActivity.this, com.rongyan.appstore.activity.port.HomePageActivity.this, Constants.HTTP_SETTINGS_URL, mHandler);
            mSettingUtils.start();
        }
    }

    @Override
    public void setResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            finishEnd();
            if(value.indexOf("settings") != -1) {
                SettingItem item = (SettingItem) JsonUtils
                        .jsonToBean(value, SettingItem.class);
                if (item != null && item.isSuccess()) {
                    int appsCount_old=ApplicationUtils.getApps_count();
                    int appsCount_new=item.getData().getSettings().getHomepage_applist_count();

                    int bannersCount_old=ApplicationUtils.getBanners_count();
                    int bannersCount_new=item.getData().getSettings().getHomepage_banners_count();

                    String bannerLocation_old=ApplicationUtils.getBanner_location();
                    String bannerLocation_new=item.getData().getSettings().getHomepage_banner_location();

                    int perPage_old=ApplicationUtils.getPer_page();
                    int perPage_new=item.getData().getSettings().getCategorylist_per_page();

                    int cacheExpires_old=ApplicationUtils.getCache_expires();
                    int cacheExpires_new=item.getData().getSettings().getCache_expires();

                    LogUtils.w(TAG,"Apps_count="+ApplicationUtils.getApps_count());
                    LogUtils.w(TAG,"Banners_count="+ApplicationUtils.getBanners_count());
                    LogUtils.w(TAG,"Banner_location="+ApplicationUtils.getBanner_location());
                    LogUtils.w(TAG,"Per_page="+ApplicationUtils.getPer_page());
                    LogUtils.w(TAG,"Cache_expires="+ApplicationUtils.getCache_expires());
                    if(appsCount_old!=appsCount_new){
                        ApplicationUtils.setApps_count(appsCount_new);
                        CacheUtils.putInt(getApplicationContext(),Constants.APPS_COUNT,appsCount_new);
                        if(homePageFragment!=null) {
                            homePageFragment.startTimer(homePageFragment.getHotOrNew());
                        }
                    }
                    boolean isGet=false;
                    if(bannersCount_old!=bannersCount_new){
                        ApplicationUtils.setBanners_count(bannersCount_new);
                        CacheUtils.putInt(getApplicationContext(),Constants.BANNERS_COUNT,bannersCount_new);
                        isGet=true;
                    }
                    if(!bannerLocation_old.equals(bannerLocation_new)){
                        ApplicationUtils.setBanner_location(bannerLocation_new);
                        CacheUtils.putString(getApplicationContext(),Constants.BANNER_LOCATION,bannerLocation_new);
                        isGet=true;
                    }
                    if(isGet){
                        if(homePageFragment!=null){
                            homePageFragment.startTimer(RecommendFragment.type_banner);
                        }
                    }
                    if(perPage_old!=perPage_new){
                        ApplicationUtils.setPer_page(perPage_new);
                        CacheUtils.putInt(getApplicationContext(),Constants.PER_PAGE,perPage_new);
                    }
                    if(cacheExpires_old!=cacheExpires_new){
                        ApplicationUtils.setCache_expires(cacheExpires_new);
                        CacheUtils.putInt(getApplicationContext(),Constants.CACHE_EXPIRES,cacheExpires_new);
                    }
                }
            }
        }catch(Exception e){
            ToastUtils.showToast(HomePageActivity.this, getString(R.string.network_exceptions));
            e.printStackTrace();
        }
    }

    @Override
    public void setFailedResponse(String value) {
        finishEnd();
        ToastUtils.showToast(HomePageActivity.this, getString(R.string.network_exceptions_again));
    }

    @Override
    public void setTimeoutResponse(String value) {
        if(num<3){
            startTimer();
        }else{
            finishEnd();
            ToastUtils.showToast(HomePageActivity.this, getString(R.string.network_fail_again));
        }
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }

    private void finishEnd(){
        num=0;
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                /*隐藏软键盘*/
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(inputMethodManager.isActive()&&isFinishing()){
                    inputMethodManager.hideSoftInputFromWindow(HomePageActivity.this.getCurrentFocus().getWindowToken(), 0);
                }
                mSearch=activity_Homepage_Search_Edit.getText().toString();
                if(!mSearch.equals("")&&mSearch!=null) {
                    Intent intent = new Intent(com.rongyan.appstore.activity.port.HomePageActivity.this, com.rongyan.appstore.activity.port.SearchActivity.class);
                    intent.putExtra("search_text", mSearch);
                    startActivity(intent);
                    activity_Homepage_Search_Edit.setText("");
                    activity_Homepage_Search_Edit.setFocusable(false);//隐藏焦点
                }
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return super.dispatchKeyEvent(event);
    }

    private PermissionUtils.PermissionGrant mPermissionGrant = new PermissionUtils.PermissionGrant() {
        @Override
        public void onPermissionGranted(int requestCode) {

        }
    };

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        PermissionUtils.requestPermissionsResult(this, requestCode, permissions, grantResults, mPermissionGrant);
    }
}
