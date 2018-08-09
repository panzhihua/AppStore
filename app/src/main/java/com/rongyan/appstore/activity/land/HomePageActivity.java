package com.rongyan.appstore.activity.land;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.rongyan.appstore.R;
import com.rongyan.appstore.activity.PermissionsActivity;
import com.rongyan.appstore.activity.PermissionsResultListener;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.fragment.land.AppsFragment;
import com.rongyan.appstore.fragment.land.ClassFragment;
import com.rongyan.appstore.fragment.land.RecommendFragment;
import com.rongyan.appstore.fragment.port.HomePageFragment;
import com.rongyan.appstore.item.AppDetailItem;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.RatingsItem;
import com.rongyan.appstore.item.SettingItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.CacheUtils;
import com.rongyan.appstore.utils.DensityUtils;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.HttpPostUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.StringUtils;
import com.rongyan.appstore.utils.ToastUtils;
import com.rongyan.appstore.widget.AppView;
import com.rongyan.appstore.widget.StarRatingView;
import com.zhy.fabridge.annotation.FCallbackId;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;
/**
 * 横屏首页
 */
public class HomePageActivity extends PermissionsActivity implements ClassFragment.selectFragment,HttpGetUtils.CallBack,HttpPostUtils.CallBack{

    private final static String TAG="HomePageActivity";

    public static final int RECOMMEND = 0;//首页推荐

    public static final int APPS =1;//分类应用

    public static final int type_setting=1,type_detail=2,type_ratings=3;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private RecommendFragment recommendFragment=null;

    private AppsFragment appsFragment=null;

    private LinearLayout homepage_Detail_Liy,homepage_Screenshots_Liy,homepage_Score_Liy,homepage_Shadow_Liy;

    private FrameLayout homepage_Shadow_Fly;

    private ScrollView homepage_Scrollview;

    private ImageView homepage_Setting_Img,homepage_Back_Img,homepage_Refresh_Img;

    private TextView homepage_Introduction_Txt,homepage_More_Txt,homepage_Graded_Txt,homepage_Score_Txt,homepage_Scorenum_Txt,homepage_Fivestar_Txt,homepage_Fourstar_Txt,homepage_Threestar_Txt,homepage_Twostar_Txt,homepage_Onestar_Txt;

    private StarRatingView homepage_Starratingview;

    private AppView homepage_Appview;

    private ClassFragment classFragment;

    private int currentIndex=-1;//记录当前Fragment编号

    private Fragment currentFragment =null;//记录当前Fragment页面

    private Timer mAppDetailTimer,mSettingTimer,mRatingsTimer;

    private HttpGetUtils mAppDetailUtils,mSettingUtils;

    private HttpPostUtils mRatingsUtils;

    private Handler mHandler = new Handler();

    private Animation in_animation,out_animation;//打开，关闭侧滑动画效果

    private int num=0;//记录网络请求次数

    private CustomDialog mCustomDialog;

    private int type_temporary=1;

    private Boolean flag=true;

    private int ratings;//评分

    private AppDetailItem.Data.App mApps;

    private String last_AppNo;//上一次点击的app_no

    private int last_refresh;//上一次刷新时间

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        init();
    }

    @Override
    public void onResume(){
        super.onResume();
        ApplicationUtils.setActivity(this);
        if(homepage_Detail_Liy.getVisibility()==View.VISIBLE){
            ApplicationUtils.setAppState(homepage_Appview,mApps);
            if(mApps.getMy_rating()!=null&&!mApps.getMy_rating().equals("")){//判断是否已经评分
                homepage_Graded_Txt.setVisibility(View.VISIBLE);
                homepage_Starratingview.setRatable(true);//可滑动
                homepage_Starratingview.setReturn(false);//不获取滑动返回结果
                homepage_Starratingview.setRate(Integer.parseInt(mApps.getMy_rating()));
                homepage_Starratingview.setRatable(false);//不可滑动
            }else {
                homepage_Graded_Txt.setVisibility(View.GONE);
                homepage_Starratingview.setReturn(false);
                homepage_Starratingview.setRatable(true);
                homepage_Starratingview.setRate(0);
                homepage_Starratingview.setReturn(true);
                if (!ApplicationUtils.isInstall(mApps)){//未评分情况下判断是否符合评分条件
                    homepage_Starratingview.setRatable(false);
                }
            }
        }
        getWindow().getDecorView().addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                //获取View可见区域的bottom
                Rect rect = new Rect();
                getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
                if(bottom!=0 && oldBottom!=0 && bottom - rect.bottom <= 0){
                    recommendFragment.getFragment_Recommend_Search_Edit().setFocusable(false);//隐藏焦点
                }else {

                }
            }
        });
    }

    public void init(){
        initView();//实例化界面控件
        initEvent();
        initData();
    }

    public void initView() {
        homepage_Setting_Img = (ImageView) findViewById(R.id.homepage_setting_img);//设置按钮
        homepage_Refresh_Img = (ImageView) findViewById(R.id.homepage_refresh_img);//刷新按钮
        {
            homepage_Scrollview = (ScrollView) findViewById(R.id.homepage_scrollview);//刷新按钮
            homepage_Detail_Liy = (LinearLayout) findViewById(R.id.homepage_detail_liy);//app详情
            homepage_Shadow_Fly = (FrameLayout) findViewById(R.id.homepage_shadow_fly);
            homepage_Shadow_Liy = (LinearLayout) findViewById(R.id.homepage_shadow_liy);
            homepage_Back_Img = (ImageView) findViewById(R.id.homepage_back_img);//返回按钮
            homepage_Appview = (AppView) findViewById(R.id.homepage_appview);
            homepage_Screenshots_Liy = (LinearLayout) findViewById(R.id.homepage_screenshots_liy);//app截图介绍
            homepage_Introduction_Txt = (TextView) findViewById(R.id.homepage_introduction_txt);//app介绍文字
            homepage_More_Txt = (TextView) findViewById(R.id.homepage_more_txt);//"更多"文字
            homepage_Starratingview = (StarRatingView) findViewById(R.id.homepage_starratingview);//滑动星星控件
            homepage_Graded_Txt = (TextView) findViewById(R.id.homepage_graded_txt);//"您已评分"文字
            homepage_Score_Txt = (TextView) findViewById(R.id.homepage_score_txt);//综合评分
            homepage_Score_Liy = (LinearLayout) findViewById(R.id.homepage_score_liy);//综合评分星星
            homepage_Scorenum_Txt = (TextView) findViewById(R.id.homepage_scorenum_txt);//评分人数
            homepage_Fivestar_Txt = (TextView) findViewById(R.id.homepage_fivestar_txt);//五星评分人数
            homepage_Fourstar_Txt = (TextView) findViewById(R.id.homepage_fourstar_txt);//四星评分人数
            homepage_Threestar_Txt = (TextView) findViewById(R.id.homepage_threestar_txt);//三星评分人数
            homepage_Twostar_Txt = (TextView) findViewById(R.id.homepage_twostar_txt);//二星评分人数
            homepage_Onestar_Txt = (TextView) findViewById(R.id.homepage_onestar_txt);//一星评分人数
        }
    }

    public void initEvent() {
        homepage_Setting_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePageActivity.this,SettingActivity.class));
            }
        });
        homepage_Refresh_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StringUtils.getSystemTime()-last_refresh>3) {
                    last_refresh=StringUtils.getSystemTime();
                    if(ApplicationUtils.getBanner_location()==null||ApplicationUtils.getBanner_location().equals("")) {
                        startTimer(type_setting, null, null,0);
                    }
                    if (currentFragment != null) {
                        if (currentFragment instanceof RecommendFragment) {
                            ((RecommendFragment) currentFragment).startTimer(((RecommendFragment) currentFragment).getHotOrNew());
                            ((RecommendFragment) currentFragment).startTimer(HomePageFragment.type_banner);
                        } else if (currentFragment instanceof AppsFragment) {
                            ((AppsFragment) currentFragment).refresh();
                        }
                        classFragment.startTimer(0);
                    }
                }
            }
        });
        homepage_Shadow_Fly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homepage_Shadow_Liy.startAnimation(out_animation);
                homepage_Detail_Liy.setVisibility(View.GONE);
                postRatings();
            }
        });
        homepage_Back_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                homepage_Shadow_Liy.startAnimation(out_animation);
                homepage_Detail_Liy.setVisibility(View.GONE);
                postRatings();
            }
        });
        homepage_More_Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    flag = false;
                    homepage_Introduction_Txt.setEllipsize(null); // 展开
                    homepage_Introduction_Txt.setSingleLine(flag);//取消lines==“3”这个固定值
                    homepage_More_Txt.setText(getString(R.string.take_back));
                } else {
                    flag = true;
                    homepage_Introduction_Txt.setEllipsize(TextUtils.TruncateAt.END); // 收缩
                    homepage_Introduction_Txt.setMaxLines(3);
                    homepage_More_Txt.setText(getString(R.string.more));
                }
            }
        });
        homepage_Starratingview.setOnRateChangeListener(new StarRatingView.OnRateChangeListener() {
            @Override
            public void onRateChange(int rate) {
                if(ApplicationUtils.isInstall(mApps)){//判断用户是否已经安装了这款APP
                    if(!homepage_Starratingview.isRatable()) {
                        homepage_Starratingview.setRatable(true);
                    }
                    ratings=rate;
                }else{
                    if(homepage_Starratingview.isRatable()) {
                        homepage_Starratingview.setRate2(0);
                        homepage_Starratingview.setRatable(false);
                    }
                    ToastUtils.showToast(HomePageActivity.this, getString(R.string.need_download_installation_score));
                }
            }
        });
    }

    public void initData() {
        checkPermissions(new String[]{Manifest.permission.CALL_PHONE,
                Manifest.permission.CAMERA,}, 300, new PermissionsResultListener() {
            @Override
            public void onSuccessful(int[] grantResults) {

            }

            @Override
            public void onFailure() {

            }
        });
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(HomePageActivity.this);
        if (classFragment==null){
            classFragment = new ClassFragment();
        }
        mCustomDialog=new CustomDialog(HomePageActivity.this);
        setLeftFragment(classFragment);
        executeNavEvent(RECOMMEND);
        startTimer(type_setting, null, null,0);
        in_animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.in_from_right);
        out_animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.out_to_right);
    }

    public void executeNavEvent(final int index) {
        if(currentIndex==index){
            return ;
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                switchModule(index);
            }
        });
        currentIndex=index;
    }

    /**
     * 切换fragment操作
     */
    private void switchModule(int index) {
        switch (index) {
            case RECOMMEND:
                if(recommendFragment==null){
                    recommendFragment = new RecommendFragment();
                }
                setRightFragment(recommendFragment);
                break;
            default:
                if(appsFragment==null){
                    appsFragment = new AppsFragment();
                }
                appsFragment.showApps(index);
                setRightFragment(appsFragment);
                break;
        }
    }

    private void setLeftFragment(Fragment fragment) {
        try {
            FragmentTransaction ft = getSupportFragmentManager()
                    .beginTransaction();
            if (fragment.isHidden()) {
                ft.show(fragment);
            } else {
                if (!fragment.isAdded()) {
                    ft.add(R.id.homepage_left_fly, fragment);
                } else {
                    ft.show(fragment);
                }
            }
            ft.commit();
        } catch (Throwable e) {
            e.printStackTrace();
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
                    ft.add(R.id.homepage_right_fly, fragment);
                } else {
                    ft.show(fragment);
                }
            }
            ft.commit();
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
                transaction.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void showFragment(int id) {
        executeNavEvent(id);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if(outState != null) {
            String FRAGMENTS_TAG = "android:support:fragments";
            outState.remove(FRAGMENTS_TAG);// remove掉保存的Fragment
        }
    }

    @FCallbackId(id = "APP_ITEM_ID")
    public void onItemClick(String app_no) {  //方法名任意
        last_AppNo=app_no;
        boolean contains = ApplicationUtils.getAppDetailMap().containsKey(app_no);    //判断是否包含指定的键值
        if (contains) {         //如果条件为真
            setResponseData(ApplicationUtils.getAppDetailMap().get(app_no));
        } else {
            startTimer(type_detail,app_no,null,0);
        }
    }

    public void startTimer(int type,String app_no,String data,int delay) {
        if (ApplicationUtils.ismNetWorkEnable()){
            if(type_temporary!=type){
                num=0;
                type_temporary=type;
            }else{
                num++;
            }
            if(type==type_detail) {
                if(!isFinishing()) {
                    mCustomDialog.showDailog();
                }
                if (mAppDetailTimer != null) {
                    mAppDetailTimer.cancel();
                }
                mAppDetailTimer = new Timer();
                mAppDetailTimer.schedule(new AppDetailTask(app_no), delay);
            }else if(type==type_setting){
                if(!isFinishing()) {
                    mCustomDialog.showDailog();
                }
                if (mSettingTimer != null) {
                    mSettingTimer.cancel();
                }
                mSettingTimer = new Timer();
                mSettingTimer.schedule(new SettingTask(), delay);
            }else if(type==type_ratings){
                if (mRatingsTimer != null) {
                    mRatingsTimer.cancel();
                }
                mRatingsTimer = new Timer();
                mRatingsTimer.schedule(new RatingsTask(data,app_no), delay);
            }
        }else{
            if(type!=type_ratings) {
                Intent mIntent = new Intent(com.rongyan.appstore.activity.land.HomePageActivity.this, com.rongyan.appstore.activity.NetworksActivity.class);
                startActivity(mIntent);
                ToastUtils.showToast(HomePageActivity.this, getString(R.string.network_failed_check_configuration));
            }
        }
    }

    class AppDetailTask extends TimerTask {

        private String mApp_no;

        AppDetailTask(String app_no){
            if(app_no!=null) {
                mApp_no = app_no;
            }
        }

        @Override
        public void run() {
            mAppDetailUtils = new HttpGetUtils(HomePageActivity.this, HomePageActivity.this, Constants.HTTP_APP_NO_URL+mApp_no, mHandler);
            mAppDetailUtils.start();
        }
    }

    class SettingTask extends TimerTask {

        @Override
        public void run() {
            mSettingUtils = new HttpGetUtils(HomePageActivity.this, HomePageActivity.this, Constants.HTTP_SETTINGS_URL, mHandler);
            mSettingUtils.start();
        }
    }

    class RatingsTask extends TimerTask {

        private String mData;

        private String mApp_no;

        RatingsTask(String data,String app_no){
            this.mData=data;
            this.mApp_no=app_no;
        }
        @Override
        public void run() {
            mRatingsUtils = new HttpPostUtils(HomePageActivity.this, HomePageActivity.this,String.format(Constants.HTTP_RATINGS_URL, mApp_no), mHandler,mData);
            mRatingsUtils.start();
        }
    }

    @Override
    public void setResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            num=0;
            mCustomDialog.hideDailog();
            if (value.indexOf("settings") == -1) {
                AppDetailItem item = (AppDetailItem) JsonUtils
                        .jsonToBean(value, AppDetailItem.class);
                if (item != null && item.isSuccess()) {
                    if(item.getData()!=null){
                        homepage_Detail_Liy.setVisibility(View.VISIBLE);
                        homepage_Shadow_Liy.startAnimation(in_animation);
                        AppDetailItem.Data.App app=item.getData().getApp();
                        if(app!=null) {
                            ApplicationUtils.getAppDetailMap().put(last_AppNo,value);
                            ratings=0;//初始化评分
                            mApps=app;
                            homepage_Appview.setView(null,app);
                            ApplicationUtils.setAppState(homepage_Appview,app);
                            flag = true;
                            homepage_Introduction_Txt.setText(app.getIntroduce());
                            homepage_Introduction_Txt.post(new Runnable() {
                                @Override
                                public void run() {
                                    if(judgeFull(homepage_Introduction_Txt)){
                                        homepage_More_Txt.setVisibility(View.VISIBLE);
                                        homepage_Introduction_Txt.setMaxLines(3);
                                        homepage_Introduction_Txt.setEllipsize(TextUtils.TruncateAt.END);
                                    }else {
                                        homepage_More_Txt.setVisibility(View.GONE);
                                    }
                                }
                            });
                            addGroupImage(app.getScreenshots());
                            if(app.getMy_rating()!=null&&!app.getMy_rating().equals("")){//判断是否已经评分
                                homepage_Graded_Txt.setVisibility(View.VISIBLE);
                                homepage_Starratingview.setRatable(true);//可滑动
                                homepage_Starratingview.setReturn(false);//不获取滑动返回结果
                                homepage_Starratingview.setRate(Integer.parseInt(app.getMy_rating()));
                                homepage_Starratingview.setRatable(false);//不可滑动
                            }else {
                                homepage_Graded_Txt.setVisibility(View.GONE);
                                homepage_Starratingview.setReturn(false);
                                homepage_Starratingview.setRatable(true);
                                homepage_Starratingview.setRate(0);
                                homepage_Starratingview.setReturn(true);
                                if (!ApplicationUtils.isInstall(app)){//未评分情况下判断是否符合评分条件
                                    homepage_Starratingview.setRatable(false);
                                }
                            }
                            int rating;
                            if(app.getRatings_count()!=0) {
                                rating = app.getRatings_sum() / app.getRatings_count();
                            }else{
                                rating=0;
                            }
                            homepage_Score_Txt.setText(rating+getString(R.string.fractions));
                            homepage_Score_Liy.removeAllViews();
                            for (int i = 0; i < rating; i++) {
                                View view = LayoutInflater.from(getApplicationContext()).inflate(
                                        R.layout.view_app_star, null);
                                homepage_Score_Liy.addView(view);
                            }
                            for (int i = rating; i < 5; i++) {
                                View view = LayoutInflater.from(getApplicationContext()).inflate(
                                        R.layout.view_app_unstar, null);
                                homepage_Score_Liy.addView(view);
                            }
                            homepage_Scorenum_Txt.setText(app.getRatings_count()+getString(R.string.scores));
                            homepage_Fivestar_Txt .setText(app.getRatings().getFive()+getString(R.string.scores));
                            homepage_Fourstar_Txt.setText(app.getRatings().getFour()+getString(R.string.scores));
                            homepage_Threestar_Txt.setText(app.getRatings().getThree()+getString(R.string.scores));
                            homepage_Twostar_Txt .setText(app.getRatings().getTwo()+getString(R.string.scores));
                            homepage_Onestar_Txt .setText(app.getRatings().getOne()+getString(R.string.scores));
                        }
                        homepage_Scrollview.fullScroll(ScrollView.FOCUS_UP);//滚动到顶部
                    }else{
                        last_AppNo=null;
                        ToastUtils.showToast(HomePageActivity.this, getString(R.string.no_details));
                    }
                }else{
                    last_AppNo=null;
                    ToastUtils.showToast(HomePageActivity.this, getString(R.string.no_details));
                }
            }else{
                final SettingItem item = (SettingItem) JsonUtils
                        .jsonToBean(value, SettingItem.class);
                if (item != null && item.isSuccess()) {
//                    if(StringUtils.compareVersion(HomePageActivity.this,item.getData().getSettings().getApp_version_code())){
//                        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//                        // 设置提示框的标题
//                        builder.setTitle(getString(R.string.apply_update)).
//                                // 设置提示框的图标
//                                setIcon(R.drawable.ic_launcher).
//                                // 设置要显示的信息
//                                setMessage(getString(R.string.please_upgrade)).
//                                // 设置确定按钮
//                                setPositiveButton(getString(R.string.sure), new DialogInterface.OnClickListener() {
//                                @Override
//                                public void onClick(DialogInterface dialog, int which) {
//                                    if (ApplicationUtils.ismNetWorkEnable()) {
//                                        new OkHttpDownLoadAPKUtils(HomePageActivity.this, item.getData().getSettings().getApp_package_url()).download();
//                                    }else{
//                                        ToastUtils.showToast(HomePageActivity.this, getString(R.string.network_failed_check_configuration));
//                                    }
//                                }
//                            });
//                        // 生产对话框
//                        AlertDialog alertDialog = builder.create();
//                        alertDialog.setCancelable(false);//点击其他区域不消失
//                        // 显示对话框
//                        alertDialog.show();
//                    }
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
                        if(recommendFragment!=null) {
                            recommendFragment.startTimer(recommendFragment.getHotOrNew());
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
                        if(recommendFragment!=null){
                            recommendFragment.startTimer(RecommendFragment.type_banner);
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

    private void addGroupImage(String[] screenshots){
        homepage_Screenshots_Liy.removeAllViews();  //清空布局文件
        LinearLayout.LayoutParams lp =new LinearLayout.LayoutParams(DensityUtils.dip2px(HomePageActivity.this,186), LinearLayout.LayoutParams.MATCH_PARENT);
        lp.setMargins(0,0, DensityUtils.dip2px(HomePageActivity.this,34),0);
        for (int i = 0; i <screenshots.length; i++) {
            final ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(lp);  //设置图片宽高
            Glide.with(HomePageActivity.this)
                    .load(screenshots[i])
                    .into(imageView);
            homepage_Screenshots_Liy.addView(imageView); //动态添加图片
        }
    }

    /**
     * 提交评分信息
     */
    private void postRatings(){
        try {
            if(ratings!=0) {
                HashMap<String, Integer> map = new HashMap<>();
                map.put("rating", ratings);//评分
                String mString = JsonUtils.beanToJson(map);
                startTimer(type_ratings, mApps.getNo(),mString,0);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setFailedResponse(String value) {
        num=0;
        mCustomDialog.hideDailog();
        ToastUtils.showToast(HomePageActivity.this, getString(R.string.network_exceptions_again)+value);
    }

    @Override
    public void setTimeoutResponse(String value) {
        if(num<3){
            startTimer(type_temporary,last_AppNo,null,0);
        }else{
            num=0;
            mCustomDialog.hideDailog();
            ToastUtils.showToast(HomePageActivity.this, getString(R.string.network_fail_again)+value);
        }
    }

    @Override
    public void setPostResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            num=0;
            RatingsItem item = (RatingsItem) JsonUtils
                    .jsonToBean(value, RatingsItem.class);
            if (item != null && item.isSuccess()) {
                ApplicationUtils.getAppDetailMap().remove(last_AppNo);
                AppInfo appInfo=new AppInfo();
                appInfo.setApp_no(last_AppNo);
                if(mDataBaseOpenHelper.QueryBeingApp(last_AppNo) ){//判断数据库中是否已存在
                    if(item.getData().getRatings_count()>0) {
                        appInfo.setRatings(item.getData().getRatings_sum() / item.getData().getRatings_count());
                    }else{
                        appInfo.setRatings(0);
                    }
                    mDataBaseOpenHelper.UpdateAppRatings(appInfo);//更新
                }else{
                    if(item.getData().getRatings_count()>0) {
                        appInfo.setRatings(item.getData().getRatings_sum() / item.getData().getRatings_count());
                    }else{
                        appInfo.setRatings(0);
                    }
                    appInfo.setInstalled_times(null);
                    appInfo.setInstalling(0);
                    mDataBaseOpenHelper.AddApp(appInfo);//添加
                }
                Intent intent = new Intent();
                intent.setAction("action.update.ratings");
                intent.putExtra("ratings_sum",item.getData().getRatings_sum());
                intent.putExtra("ratings_count",item.getData().getRatings_count());
                intent.putExtra("app_no", last_AppNo);
                sendBroadcast(intent);
                if(currentFragment!=null){
                    currentFragment.onResume();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setPostFailedResponse(String value) {
        num=0;
    }

    @Override
    public void setPostTimeoutResponse(String value) {
        num=0;
    }

    /**
     * 判断数据是否填满了TextView前三行
     */
    private boolean judgeFull(TextView tv_content){
        return tv_content.getPaint().measureText(tv_content.getText().toString()) > 3*(tv_content.getWidth() -
                tv_content.getPaddingRight() - tv_content.getPaddingLeft());
    }

    public ClassFragment getClassFragment() {
        return classFragment;
    }

    public Fragment getCurrentFragment() {
        return currentFragment;
    }


    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        try {
            if (event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            /*隐藏软键盘*/
                InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (inputMethodManager.isActive()&&isFinishing()) {
                    inputMethodManager.hideSoftInputFromWindow(HomePageActivity.this.getCurrentFocus().getWindowToken(), 0);
                }
                if (!recommendFragment.getSearch().equals("") && recommendFragment.getSearch() != null) {
                    Intent intent = new Intent(HomePageActivity.this, SearchActivity.class);
                    intent.putExtra("search_text", recommendFragment.getSearch());
                    startActivity(intent);
                    recommendFragment.getFragment_Recommend_Search_Edit().setText("");
                } else {
                    recommendFragment.getFragment_Recommend_Search_Edit().setFocusable(false);//隐藏焦点
                }
                return true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return super.dispatchKeyEvent(event);
    }

}
