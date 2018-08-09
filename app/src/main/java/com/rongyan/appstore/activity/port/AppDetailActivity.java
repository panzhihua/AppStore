package com.rongyan.appstore.activity.port;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.AppDetailItem;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.RatingsItem;
import com.rongyan.appstore.utils.ApplicationUtils;
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

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;;

/**
 * app详情(竖屏)
 */

public class AppDetailActivity extends AppCompatActivity implements HttpGetUtils.CallBack,HttpPostUtils.CallBack{

    private final static String TAG="AppDetailActivity";

    public static final int type_detail=1,type_ratings=2;

    private ImageView activity_Appdetail_Back_Img,activity_Appdetail_Refresh_Img;

    private AppView activity_Appdetail_Appview;

    private LinearLayout activity_Appdetail_Screenshots_Liy,activity_Appdetail_Score_Liy,activity_Appdetail_Liy;

    private TextView activity_Appdetail_Introduction_Txt,activity_Appdetail_More_Txt,activity_Appdetail_Graded_Txt,activity_Appdetail_Score_Txt,activity_Appdetail_Scorenum_Txt,activity_Appdetail_Fivestar_Txt,activity_Appdetail_Fourstar_Txt,activity_Appdetail_Threestar_Txt,activity_Appdetail_Twostar_Txt,activity_Appdetail_Onestar_Txt;

    private StarRatingView activity_Appdetail_Starratingview;

    private Timer mAppDetailTimer,mRatingsTimer;

    private HttpGetUtils mAppDetailUtils;

    private HttpPostUtils mRatingsUtils;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private Handler mHandler = new Handler();

    private String appNo;

    private int num=0;//记录网络请求次数

    private int type_temporary=1;

    private Boolean flag=true;

    private int ratings;//评分

    private AppDetailItem.Data.App mApps;

    private CustomDialog mCustomDialog;

    private Timer timer = null;

    private boolean isFirst=true;//是否首次进入该页面

    //手指向右滑动时的最小速度
    private static final int XSPEED_MIN = 200;

    //手指向右滑动时的最小距离
    private static final int XDISTANCE_MIN = 100;

    //手指向上下滑动时的最大距离
    private static final int YDISTANCE_MAX = 50;

    //记录手指按下时的横坐标。
    private float xDown,yDown;

    //记录手指移动时的横坐标。
    private float xMove,yMove;

    //用于计算手指滑动的速度。
    private VelocityTracker mVelocityTracker;

    private int last_refresh;//上一次刷新时间
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appdetail);
        init();
    }

    @Override
    public void onResume(){
        ApplicationUtils.setActivity(this);
        if(!isFirst){
            if(mApps!=null){
                ApplicationUtils.setAppState(activity_Appdetail_Appview, mApps);
                activity_Appdetail_Appview.setAppInfo();
                if (mApps.getMy_rating() != null && !mApps.getMy_rating().equals("")) {//判断是否已经评分
                    activity_Appdetail_Graded_Txt.setVisibility(View.VISIBLE);
                    activity_Appdetail_Starratingview.setRatable(true);//可滑动
                    activity_Appdetail_Starratingview.setReturn(false);//不获取滑动返回结果
                    activity_Appdetail_Starratingview.setRate(Integer.parseInt(mApps.getMy_rating()));
                    activity_Appdetail_Starratingview.setRatable(false);//不可滑动
                } else {
                    activity_Appdetail_Graded_Txt.setVisibility(View.GONE);
                    activity_Appdetail_Starratingview.setReturn(false);
                    activity_Appdetail_Starratingview.setRatable(true);
                    activity_Appdetail_Starratingview.setRate(0);
                    activity_Appdetail_Starratingview.setReturn(true);
                    if (!ApplicationUtils.isInstall(mApps)) {//未评分情况下判断是否符合评分条件
                        activity_Appdetail_Starratingview.setRatable(false);
                    }
                }
            }
        }
        isFirst=false;
        super.onResume();
    }

    private void init(){
        initView();//实例化界面控件
        initEvent();
        initData();
    }

    private void initView(){
        activity_Appdetail_Liy = (LinearLayout) findViewById(R.id.activity_appdetial_liy);
        activity_Appdetail_Back_Img = (ImageView) findViewById(R.id.activity_appdetail_back_img);
        activity_Appdetail_Refresh_Img = (ImageView) findViewById(R.id.activity_appdetial_refresh_img);
        activity_Appdetail_Appview = (AppView) findViewById(R.id.activity_appdetail_appview);
        activity_Appdetail_Screenshots_Liy = (LinearLayout) findViewById(R.id.activity_appdetail_screenshots_liy);//app截图介绍
        activity_Appdetail_Introduction_Txt = (TextView) findViewById(R.id.activity_appdetail_introduction_txt);//app介绍文字
        activity_Appdetail_More_Txt = (TextView) findViewById(R.id.activity_appdetail_more_txt);//"更多"文字
        activity_Appdetail_Starratingview = (StarRatingView) findViewById(R.id.activity_appdetail_starratingview);//滑动星星控件
        activity_Appdetail_Graded_Txt = (TextView) findViewById(R.id.activity_appdetail_graded_txt);//"您已评分"文字
        activity_Appdetail_Score_Txt = (TextView) findViewById(R.id.activity_appdetail_score_txt);//综合评分
        activity_Appdetail_Score_Liy = (LinearLayout) findViewById(R.id.activity_appdetail_score_liy);//综合评分星星
        activity_Appdetail_Scorenum_Txt = (TextView) findViewById(R.id.activity_appdetail_scorenum_txt);//评分人数
        activity_Appdetail_Fivestar_Txt = (TextView) findViewById(R.id.activity_appdetail_fivestar_txt);//五星评分人数
        activity_Appdetail_Fourstar_Txt = (TextView) findViewById(R.id.activity_appdetail_fourstar_txt);//四星评分人数
        activity_Appdetail_Threestar_Txt = (TextView) findViewById(R.id.activity_appdetail_threestar_txt);//三星评分人数
        activity_Appdetail_Twostar_Txt = (TextView) findViewById(R.id.activity_appdetail_twostar_txt);//二星评分人数
        activity_Appdetail_Onestar_Txt = (TextView) findViewById(R.id.activity_appdetail_onestar_txt);//一星评分人数
    }

    private void initEvent(){
        activity_Appdetail_Back_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        activity_Appdetail_Refresh_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StringUtils.getSystemTime()-last_refresh>3) {
                    last_refresh = StringUtils.getSystemTime();
                    startTimer(type_detail, appNo, null);
                }
            }
        });

        activity_Appdetail_More_Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    flag = false;
                    activity_Appdetail_Introduction_Txt.setEllipsize(null); // 展开
                    activity_Appdetail_Introduction_Txt.setSingleLine(flag);//取消lines==“3”这个固定值
                    activity_Appdetail_More_Txt.setText(getString(R.string.take_back));
                } else {
                    flag = true;
                    activity_Appdetail_Introduction_Txt.setEllipsize(TextUtils.TruncateAt.END); // 收缩
                    activity_Appdetail_Introduction_Txt.setMaxLines(3);
                    activity_Appdetail_More_Txt.setText(getString(R.string.more));
                }
            }
        });
        activity_Appdetail_Starratingview.setOnRateChangeListener(new StarRatingView.OnRateChangeListener() {
            @Override
            public void onRateChange(int rate) {
                if(ApplicationUtils.isInstall(mApps)){//判断用户是否已经安装了这款APP
                    if(!activity_Appdetail_Starratingview.isRatable()) {
                        activity_Appdetail_Starratingview.setRatable(true);
                    }
                    ratings=rate;
                    if (timer == null){
                        timer = new Timer();
                    } else {
                        timer.cancel();
                        timer = new Timer();
                        timer.schedule(new TimerTask()//启动一个定时器
                        {
                            public void run()
                            {
                                postRatings();
                            }
                        },5000);//5秒内无操作就提交评分记录
                    }
                }else{
                    if(activity_Appdetail_Starratingview.isRatable()) {
                        activity_Appdetail_Starratingview.setRate2(0);
                        activity_Appdetail_Starratingview.setRatable(false);
                    }
                    ToastUtils.showToast(AppDetailActivity.this, getString(R.string.need_download_installation_score));
                }
            }
        });
    }

    private void initData(){
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getApplicationContext());
        appNo=getIntent().getStringExtra("app_no");
        if(mCustomDialog==null) {
            mCustomDialog = new CustomDialog(AppDetailActivity.this);
        }
        startTimer(type_detail,appNo,null);
    }

    public void startTimer(int type,String app_no,String data) {
        if (ApplicationUtils.ismNetWorkEnable()){
            if(type_temporary!=type){
                num=0;
                type_temporary=type;
            }else{
                num++;
            }
            if(type_temporary==type_detail){
                if(!isFinishing()) {
                    mCustomDialog.showDailog();
                }
            }
            if(type==type_detail) {
                if (mAppDetailTimer != null) {
                    mAppDetailTimer.cancel();
                }
                mAppDetailTimer = new Timer();
                mAppDetailTimer.schedule(new AppDetailTask(app_no), 0);
            }else if(type==type_ratings){
                if (mRatingsTimer != null) {
                    mRatingsTimer.cancel();
                }
                mRatingsTimer = new Timer();
                mRatingsTimer.schedule(new RatingsTask(data,app_no), 0);
            }
        }else{
            if(type_temporary==type_detail) {
                ToastUtils.showToast(getApplicationContext(), getString(R.string.network_failed_check_configuration));
            }
        }
    }

    class AppDetailTask extends TimerTask {

        private String mApp_no;

        AppDetailTask(String app_no){
            mApp_no=app_no;
        }

        @Override
        public void run() {
            mAppDetailUtils = new HttpGetUtils(AppDetailActivity.this, AppDetailActivity.this, Constants.HTTP_APP_NO_URL+mApp_no, mHandler);
            mAppDetailUtils.start();
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
            mRatingsUtils = new HttpPostUtils(AppDetailActivity.this, AppDetailActivity.this,String.format(Constants.HTTP_RATINGS_URL, mApp_no), mHandler,mData);
            mRatingsUtils.start();
        }
    }

    @Override
    public void setResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            finish(type_detail);
            AppDetailItem item = (AppDetailItem) JsonUtils
                    .jsonToBean(value, AppDetailItem.class);
            if (item != null && item.isSuccess()) {
                if (item.getData() != null) {
                    AppDetailItem.Data.App app = item.getData().getApp();
                    if (app != null) {
                        ratings = 0;//初始化评分
                        mApps = app;
                        activity_Appdetail_Appview.setView(null, app);
                        ApplicationUtils.setAppState(activity_Appdetail_Appview, app);
                        flag = true;
                        activity_Appdetail_Introduction_Txt.setText(app.getIntroduce());
                        activity_Appdetail_Introduction_Txt.post(new Runnable() {
                            @Override
                            public void run() {
                                if (judgeFull(activity_Appdetail_Introduction_Txt)) {
                                    activity_Appdetail_More_Txt.setVisibility(View.VISIBLE);
                                    activity_Appdetail_Introduction_Txt.setMaxLines(3);
                                    activity_Appdetail_Introduction_Txt.setEllipsize(TextUtils.TruncateAt.END);
                                } else {
                                    activity_Appdetail_More_Txt.setVisibility(View.GONE);
                                }
                            }
                        });
                        addGroupImage(app.getScreenshots());
                        if (app.getMy_rating() != null && !app.getMy_rating().equals("")) {//判断是否已经评分
                            activity_Appdetail_Graded_Txt.setVisibility(View.VISIBLE);
                            activity_Appdetail_Starratingview.setRatable(true);//可滑动
                            activity_Appdetail_Starratingview.setReturn(false);//不获取滑动返回结果
                            activity_Appdetail_Starratingview.setRate(Integer.parseInt(app.getMy_rating()));
                            activity_Appdetail_Starratingview.setRatable(false);//不可滑动
                        } else {
                            activity_Appdetail_Graded_Txt.setVisibility(View.GONE);
                            activity_Appdetail_Starratingview.setReturn(false);
                            activity_Appdetail_Starratingview.setRatable(true);
                            activity_Appdetail_Starratingview.setRate(0);
                            activity_Appdetail_Starratingview.setReturn(true);
                            if (!ApplicationUtils.isInstall(app)) {//未评分情况下判断是否符合评分条件
                                activity_Appdetail_Starratingview.setRatable(false);
                            }
                        }
                        int rating;
                        if (app.getRatings_count() != 0) {
                            rating = app.getRatings_sum() / app.getRatings_count();
                        } else {
                            rating = 0;
                        }
                        activity_Appdetail_Score_Txt.setText(rating + getString(R.string.fractions));
                        activity_Appdetail_Score_Liy.removeAllViews();
                        for (int i = 0; i < rating; i++) {
                            View view = LayoutInflater.from(getApplicationContext()).inflate(
                                    R.layout.view_app_star, null);
                            activity_Appdetail_Score_Liy.addView(view);
                        }
                        for (int i = rating; i < 5; i++) {
                            View view = LayoutInflater.from(getApplicationContext()).inflate(
                                    R.layout.view_app_unstar, null);
                            activity_Appdetail_Score_Liy.addView(view);
                        }
                        activity_Appdetail_Scorenum_Txt.setText(app.getRatings_count() + getString(R.string.scores));
                        activity_Appdetail_Fivestar_Txt.setText(app.getRatings().getFive() + getString(R.string.scores));
                        activity_Appdetail_Fourstar_Txt.setText(app.getRatings().getFour() + getString(R.string.scores));
                        activity_Appdetail_Threestar_Txt.setText(app.getRatings().getThree() + getString(R.string.scores));
                        activity_Appdetail_Twostar_Txt.setText(app.getRatings().getTwo() + getString(R.string.scores));
                        activity_Appdetail_Onestar_Txt.setText(app.getRatings().getOne() + getString(R.string.scores));
                        activity_Appdetail_Liy.setVisibility(View.VISIBLE);
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            ToastUtils.showToast(getApplicationContext(), getString(R.string.network_exceptions)+e.toString());
        }
    }

    @Override
    public void setFailedResponse(String value) {
        finish(type_detail);
        ToastUtils.showToast(getApplicationContext(), getString(R.string.network_exceptions_again)+value);
    }

    @Override
    public void setTimeoutResponse(String value) {
        if(num<3){
            startTimer(type_detail,appNo,null);
        }else{
            finish(type_detail);
            ToastUtils.showToast(getApplicationContext(), getString(R.string.network_fail_again)+value);
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
                startTimer(type_ratings, mApps.getNo(),mString);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void setPostResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            RatingsItem item = (RatingsItem) JsonUtils
                    .jsonToBean(value, RatingsItem.class);
            if (item != null && item.isSuccess()) {
                activity_Appdetail_Graded_Txt.setVisibility(View.VISIBLE);
                activity_Appdetail_Starratingview.setReturn(false);//不获取滑动返回结果
                activity_Appdetail_Starratingview.setRatable(false);//不可滑动
                AppInfo appInfo=new AppInfo();
                appInfo.setApp_no(mApps.getNo());
                if(mDataBaseOpenHelper.QueryBeingApp(mApps.getNo()) ){//判断数据库中是否已存在
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
                intent.putExtra("app_no", mApps.getNo());
                sendBroadcast(intent);
            }
        }catch(Exception e){
            e.printStackTrace();
            finish();
        }
    }

    @Override
    public void setPostFailedResponse(String value) {
        LogUtils.w(TAG,value);
    }

    @Override
    public void setPostTimeoutResponse(String value) {
        LogUtils.w(TAG,value);
    }

    /**
     * 判断数据是否填满了TextView前三行
     */
    private boolean judgeFull(TextView tv_content){
        return tv_content.getPaint().measureText(tv_content.getText().toString()) > 3*(tv_content.getWidth() -
                tv_content.getPaddingRight() - tv_content.getPaddingLeft());
    }

    private void addGroupImage(String[] screenshots){
        activity_Appdetail_Screenshots_Liy.removeAllViews();  //清空布局文件
        LinearLayout.LayoutParams lp =new LinearLayout.LayoutParams(DensityUtils.dip2px(AppDetailActivity.this,182), LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0,0, DensityUtils.dip2px(AppDetailActivity.this,10),0);
        for (int i = 0; i <screenshots.length; i++) {
            final ImageView imageView = new ImageView(this);
            imageView.setLayoutParams(lp);  //设置图片宽高
            Glide.with(AppDetailActivity.this)
                    .load(screenshots[i])
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            activity_Appdetail_Screenshots_Liy.addView(imageView); //动态添加图片
                            return false;
                        }
            }).into(imageView);
        }
    }

    private void finish(int state){
        num=0;
        if(state==type_detail){
            mCustomDialog.hideDailog();
        }
    }

}
