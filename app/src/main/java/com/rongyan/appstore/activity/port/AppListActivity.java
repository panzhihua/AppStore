package com.rongyan.appstore.activity.port;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
import com.rongyan.appstore.adapter.AppAdapter;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.AppClassItem;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.StringUtils;
import com.rongyan.appstore.utils.ToastUtils;
import com.rongyan.appstore.widget.AppView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;;

/**
 * app分类详情activity(竖屏)
 */

public class AppListActivity extends AppCompatActivity implements HttpGetUtils.CallBack,AppView.app{

    private final static String TAG="AppsFragment";

    private int state_null=0,state_init=1,state_refresh=2,state_load=3;

    private PullToRefreshLayout activity_Applist_Pulltorefreshlayout;

    private ImageView activity_Applist_Back_Img,activity_Applist_Refresh_Img;

    private TextView activity_Applist_Name_Txt,activity_Applist_Txt;

    private LinearLayout activity_Applist_Liy;

    private ListView activity_Applist_Listview;

    private AppAdapter mAppAdapter;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private Timer mAppsTimer;

    private HttpGetUtils mAppsUtils;

    private Handler mHandler = new Handler();

    private int lastId=-1;

    private int pageNum=1;

    private int num=0;

    private AppClassItem mAppClassItem;

    private List<Apps> mAppsList;

    private int state=state_init;

    private CustomDialog mCustomDialog;

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

    private boolean isLoading=false;//是否正在加载中
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);
        init();
    }

    @Override
    public void onResume(){
        super.onResume();
        ApplicationUtils.setActivity(this);
        if(!isFirst){
            mAppAdapter.notifyDataSetChanged();
        }
        isFirst=false;
    }

    private void init(){
        initView();//实例化界面控件
        initEvent();
        initData();
    }

    private void initView(){
        activity_Applist_Pulltorefreshlayout = (PullToRefreshLayout)findViewById(R.id.activity_applist_pulltorefreshlayout);
        activity_Applist_Back_Img = (ImageView) findViewById(R.id.activity_applist_back_img);
        activity_Applist_Name_Txt = (TextView) findViewById(R.id.activity_applist_name_txt);
        activity_Applist_Refresh_Img = (ImageView) findViewById(R.id.activity_applist_refresh_img);
        activity_Applist_Listview = (ListView) findViewById(R.id.activity_applist_listview);
        activity_Applist_Liy = (LinearLayout) findViewById(R.id.activity_applist_liy);
        activity_Applist_Txt = (TextView) findViewById(R.id.activity_applist_txt);
    }

    private void initEvent(){
//        activity_Applist_Listview.setOnTouchListener(this);
        activity_Applist_Back_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        activity_Applist_Refresh_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(StringUtils.getSystemTime()-last_refresh>3) {
                    last_refresh = StringUtils.getSystemTime();
                    state = state_init;
                    mDataBaseOpenHelper.DeleteClass(lastId);//删除相应类别id下所有内容
                    refreshView();
                    startTimer();
                }
            }
        });
        activity_Applist_Pulltorefreshlayout.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
                if(isLoading){
                    activity_Applist_Pulltorefreshlayout.finishRefresh();
                    return;
                }
                state=state_refresh;
                refreshView();
                startTimer();
            }

            @Override
            public void loadMore() {
                if(mAppClassItem!=null) {
                    if (!mAppClassItem.getData().getPaging().isIs_last_page()) {//判断是不是最后一页
                        if(isLoading){
                            activity_Applist_Pulltorefreshlayout.finishLoadMore();
                            return;
                        }
                        state = state_load;
                        pageNum++;
                        startTimer();
                    } else {
                        state = state_load;
                        ToastUtils.showToast(getApplicationContext(), getString(R.string.no_more));
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 结束加载更多
                                activity_Applist_Pulltorefreshlayout.finishLoadMore();
                            }
                        }, 0);
                    }
                }else{
                    // 结束加载更多
                    activity_Applist_Pulltorefreshlayout.finishLoadMore();
                }
            }
        });
    }

    private void initData(){
        int id=getIntent().getIntExtra("id",-1);
        String name=getIntent().getStringExtra("name");
        activity_Applist_Name_Txt.setText(name);
        mAppAdapter = new AppAdapter(AppListActivity.this, getApplicationContext());
        activity_Applist_Listview.setAdapter(mAppAdapter);
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getApplicationContext());
        if(mCustomDialog==null) {
            mCustomDialog = new CustomDialog(AppListActivity.this);
        }
        if(id>0) {
            showApps(id);
        }
    }

    private void refreshView(){
        pageNum=1;
        mAppsList=new ArrayList<>();
    }

    public void startTimer() {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            if(state==state_init){
                if(!isFinishing()) {
                    mCustomDialog.showDailog();
                }
            }
            if (mAppsTimer != null) {
                mAppsTimer.cancel();
            }
            mAppsTimer = new Timer();
            mAppsTimer.schedule(new AppsTask(),0);
        }else{
            ToastUtils.showToast(getApplicationContext(), getString(R.string.network_failed_check_configuration));
        }
    }

    class AppsTask extends TimerTask {
        @Override
        public void run() {
            mAppsUtils = new HttpGetUtils(getApplicationContext(),AppListActivity.this, String.format(Constants.HTTP_APPS_URL, lastId,pageNum,ApplicationUtils.getPer_page()), mHandler);
            mAppsUtils.start();
        }
    }

    public void showApps(int id) {
        if(lastId!=id) {
            lastId=id;
            refreshView();
            if(mDataBaseOpenHelper.QueryOverdueClass(lastId,pageNum)){//判断类别内容是否已过期，如果已过期则需要重新获取
                mDataBaseOpenHelper.DeleteClass(lastId);//删除相应类别id下所有内容
                startTimer();
            }else{
                List<String> valueList=mDataBaseOpenHelper.QueryClass(id);
                if(valueList!=null&&!valueList.isEmpty()) {
                    for(String value:valueList) {
                        state=state_null;
                        setResponseData(value);
                    }
                }else{
                    startTimer();
                }
            }
        }
    }

    @Override
    public void setResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            finish(state);
            AppClassItem item = (AppClassItem) JsonUtils
                    .jsonToBean(value, AppClassItem.class);
            if (item != null && item.isSuccess()) {
                if (item.getData() != null) {
                    if (item.getData().getApps() != null && item.getData().getApps().length > 0) {
                        if (mDataBaseOpenHelper.QueryBeingClass(lastId, item.getData().getPaging().getCurrent_page())) {//判断数据库中是否已存在
                            mDataBaseOpenHelper.UpdateClass(lastId, value, item.getData().getPaging().getCurrent_page());//更新
                        } else {
                            mDataBaseOpenHelper.AddClass(lastId, value, item.getData().getPaging().getCurrent_page());//添加
                        }
                        mAppClassItem = item;
                        for (int i = 0; i < item.getData().getApps().length; i++) {
                            Apps app = item.getData().getApps()[i];
                            if (app != null) {
                                boolean isLike = false;
                                for (Apps apps : mAppsList) {
                                    if (apps.getNo().equals(app.getNo())) {
                                        isLike = true;
                                        break;
                                    }
                                }
                                if (!isLike) {
                                    mAppsList.add(app);
                                }
                                if (ApplicationUtils.getAppMap().get("package:" + app.getPackage_name()) == null) {
                                    ApplicationUtils.setAppMap("package:" + app.getPackage_name(), app);
                                }
                                if (state != state_null) {
                                    AppInfo appInfo;
                                    if (app.getRatings_count() > 0) {
                                        appInfo = new AppInfo(app.getNo(), app.getInstalled_times(), (app.getRatings_sum() / app.getRatings_count()),0);
                                    } else {
                                        appInfo = new AppInfo(app.getNo(), app.getInstalled_times(), 0,0);
                                    }
                                    if (mDataBaseOpenHelper.QueryBeingApp(app.getNo())) {//判断数据库中是否已存在
                                        mDataBaseOpenHelper.UpdateApp(appInfo);//更新
                                    } else {
                                        mDataBaseOpenHelper.AddApp(appInfo);//添加
                                    }
                                }
                            }
                        }
                        mAppAdapter.setList(mAppsList);
                        setView(true,0);
                    }else{
                        setView(false,2);
                    }
                }else{
                    setView(false,2);
                }
            }else{
                setView(false,1);
            }
        } catch (Exception e) {
            e.printStackTrace();
            setView(false,1);
            ToastUtils.showToast(getApplicationContext(), getString(R.string.network_exceptions));
            if (pageNum > 1) {
                pageNum--;
            }
        }
    }

    @Override
    public void setFailedResponse(String value) {
        finish(state);
        setView(false,1);
        if(pageNum>1){
            pageNum--;
        }
        ToastUtils.showToast(getApplicationContext(), getString(R.string.network_exceptions_again));
    }

    @Override
    public void setTimeoutResponse(String value) {
        if(num<3){
            startTimer();
        }else{
            finish(state);
            setView(false,1);
            if(pageNum>1){
                pageNum--;
            }
            ToastUtils.showToast(getApplicationContext(), getString(R.string.network_fail_again));
        }
    }

    private void finish(int state){
        num=0;
        isLoading=false;
        if(state==state_load){
            // 结束加载更多
            activity_Applist_Pulltorefreshlayout.finishLoadMore();
        }else if(state==state_refresh){
            // 结束刷新
            activity_Applist_Pulltorefreshlayout.finishRefresh();
        }else if(state==state_init){
            mCustomDialog.hideDailog();
        }
    }

    private void setView(boolean result,int type){
        if(result){
            activity_Applist_Liy.setVisibility(View.GONE);
            activity_Applist_Pulltorefreshlayout.setVisibility(View.VISIBLE);
        }else{
            activity_Applist_Liy.setVisibility(View.VISIBLE);
            activity_Applist_Pulltorefreshlayout.setVisibility(View.GONE);
            if(type==1){
                activity_Applist_Txt.setText(R.string.shopkeeper_busy_system_tired);
            }else if(type==2){
                activity_Applist_Txt.setText(R.string.no_application_under_classification);
            }
        }
    }

    @Override
    public void getAppNo(String app_no) {
        Intent intent = new Intent(AppListActivity.this, AppDetailActivity.class);
        intent.putExtra("app_no",app_no);
        startActivity(intent);
    }

//    @Override
//    public boolean onTouch(View v, MotionEvent event) {
//        createVelocityTracker(event);
//        switch (event.getAction()) {
//            case MotionEvent.ACTION_DOWN:
//                xDown = event.getRawX();
//                yDown = event.getRawY();
//                break;
//            case MotionEvent.ACTION_MOVE:
//                xMove = event.getRawX();
//                yMove = event.getRawY();
//                //活动的距离
//                int distanceX = (int) (xMove - xDown);
//                int distanceY = (int) (yMove - yDown);
//                //获取顺时速度
//                int xSpeed = getScrollVelocity();
//                //当滑动的距离大于我们设定的最小距离且滑动的瞬间速度大于我们设定的速度时，返回到上一个activity
//                if(distanceX > XDISTANCE_MIN && xSpeed > XSPEED_MIN&&YDISTANCE_MAX>Math.abs(distanceY)) {
//                    finish();
//                    overridePendingTransition(R.anim.in_from_left, R.anim.out_to_right);
//                }
//                break;
//            case MotionEvent.ACTION_UP:
//                recycleVelocityTracker();
//                break;
//            default:
//                break;
//        }
//        return false;
//    }
//
//    /**
//     *创建VelocityTracker对象，并将触摸content界面的滑动事件加入到VelocityTracker当中。
//     */
//    private void createVelocityTracker(MotionEvent event) {
//        if (mVelocityTracker == null) {
//            mVelocityTracker = VelocityTracker.obtain();
//        }
//        mVelocityTracker.addMovement(event);
//    }
//
//    /**
//     * 回收VelocityTracker对象。
//     */
//    private void recycleVelocityTracker() {
//        mVelocityTracker.recycle();
//        mVelocityTracker = null;
//    }
//
//    /**
//     * 获取手指在content界面滑动的速度。
//     *
//     * @return 滑动速度，以每秒钟移动了多少像素值为单位。
//     */
//    private int getScrollVelocity() {
//        mVelocityTracker.computeCurrentVelocity(1000);
//        int velocity = (int) mVelocityTracker.getXVelocity();
//        return Math.abs(velocity);
//    }
}
