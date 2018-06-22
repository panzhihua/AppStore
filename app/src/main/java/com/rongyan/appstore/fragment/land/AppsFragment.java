package com.rongyan.appstore.fragment.land;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.jwenfeng.library.pulltorefresh.BaseRefreshListener;
import com.jwenfeng.library.pulltorefresh.PullToRefreshLayout;
import com.rongyan.appstore.adapter.AppsAdapter;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.AppClassItem;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.item.LeftOrRightAppsItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.ToastUtils;
import com.rongyan.appstore.widget.AppView;
import com.zhy.fabridge.lib.Fabridge;


import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;

/**
 * 横屏分类应用
 */
public class AppsFragment extends Fragment implements HttpGetUtils.CallBack,AppView.app {

    private final static String TAG="AppsFragment";

    private int state_null=0,state_init=1,state_refresh=2,state_load=3;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private ListView fragment_Apps_Listview;

    private PullToRefreshLayout fragment_Apps_Pulltorefreshlayout;

    private LinearLayout fragment_Apps_Liy;

    private TextView fragment_Apps_Txt;

    private AppsAdapter mAppsAdapter;

    private Timer mAppsTimer;

    private HttpGetUtils mAppsUtils;

    private CustomDialog mCustomDialog;

    private Handler mHandler = new Handler();

    private int lastId=-1,firstId=-1;

    private int num=0;

    private int pageNum=1;

    private AppClassItem mAppClassItem;

    private List<LeftOrRightAppsItem> mLeftOrRightAppsItem;

    private int state=state_init;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    private boolean isFirst=true;//是否首次进入该页面

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_apps, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onResume(){
        if(!isFirst){
            mAppsAdapter.mCount=0;
            mAppsAdapter.notifyDataSetChanged();
        }
        isFirst=false;
        super.onResume();
    }

    @Override
    public void onPause(){
        super.onPause();
    }

    public void init(){
        initView();
        initEvent();
        initData();
    }

    public void initView(){
        fragment_Apps_Pulltorefreshlayout = (PullToRefreshLayout) getView().findViewById(R.id.fragment_apps_pulltorefreshlayout);
        fragment_Apps_Liy = (LinearLayout) getView().findViewById(R.id.fragment_apps_liy);
        fragment_Apps_Txt = (TextView) getView().findViewById(R.id.fragment_apps_txt);
        fragment_Apps_Listview = (ListView) getView().findViewById(R.id.fragment_apps_listview);
    }

    public void initEvent(){
        fragment_Apps_Pulltorefreshlayout.setRefreshListener(new BaseRefreshListener() {
            @Override
            public void refresh() {
                state=state_refresh;
                refreshView();
                startTimer();
            }

            @Override
            public void loadMore() {
                if(mAppClassItem!=null) {
                    if (!mAppClassItem.getData().getPaging().isIs_last_page()) {//判断是不是最后一页
                        state = state_load;
                        pageNum++;
                        startTimer();
                        fragment_Apps_Listview.setSelection(mLeftOrRightAppsItem.size()-1);
                    } else {
                        state = state_load;
                        if(isAdded()) {
                            ToastUtils.showToast(getContext(), getString(R.string.no_more));
                        }
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                // 结束加载更多
                                fragment_Apps_Pulltorefreshlayout.finishLoadMore();
                            }
                        }, 0);
                    }
                }else{
                    fragment_Apps_Pulltorefreshlayout.finishLoadMore();
                }
            }
        });
    }


    public void initData(){
        mAppsAdapter = new AppsAdapter(this, getContext());
        fragment_Apps_Listview.setAdapter(mAppsAdapter);
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getContext());
        if(mCustomDialog==null) {
            mCustomDialog = new CustomDialog(getActivity());
        }
        showApps(firstId);
    }

    private void refreshView(){
        pageNum=1;
        mLeftOrRightAppsItem=new ArrayList<>();
        if(fragment_Apps_Pulltorefreshlayout!=null) {
            fragment_Apps_Pulltorefreshlayout.setVisibility(View.INVISIBLE);
        }
    }

    public void startTimer() {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            if(state==state_init&&isAdded()) {
                mCustomDialog.showDailog();
            }
            if (mAppsTimer != null) {
                mAppsTimer.cancel();
            }
            mAppsTimer = new Timer();
            mAppsTimer.schedule(new AppsTask(),0);
        }else{
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_failed_check_configuration));
            }
        }
    }

    @Override
    public void getAppNo(String app_no) {
        Fabridge.call(getActivity(),"APP_ITEM_ID",app_no);  //调用ID对应的方法
    }

    class AppsTask extends TimerTask {
        @Override
        public void run() {
            mAppsUtils = new HttpGetUtils(getContext(),AppsFragment.this, String.format(Constants.HTTP_APPS_URL, lastId,pageNum,ApplicationUtils.getPer_page()), mHandler);
            mAppsUtils.start();
        }
    }

    public void refresh(){
        state=state_init;
        mDataBaseOpenHelper.DeleteClass(lastId);//删除相应类别id下所有内容
        refreshView();
        startTimer();
    }

    public void showApps(int id) {
        if(firstId==-1){
            firstId=id;
            return;
        }
        if(lastId!=id) {
            lastId=id;
            refreshView();
            if(mDataBaseOpenHelper.QueryOverdueClass(lastId,pageNum)){//判断类别内容是否已过期，如果已过期则需要重新获取
                mDataBaseOpenHelper.DeleteClass(lastId);//删除相应类别id下所有内容
                state=state_init;
                startTimer();
            }else{
                List<String> valueList=mDataBaseOpenHelper.QueryClass(id);
                if(valueList!=null&&!valueList.isEmpty()) {
                    state=state_null;
                    for(String value:valueList) {
                        setResponseData(value);
                    }
                }else{
                    state=state_init;
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
                        for (int i = 0; i < item.getData().getApps().length; i = i + 2) {
                            if (i + 1 < item.getData().getApps().length) {
                                Apps leftApp = item.getData().getApps()[i];
                                Apps rightApp = item.getData().getApps()[i + 1];
                                if (leftApp != null && rightApp != null) {
                                    boolean isLike = false;
                                    for (LeftOrRightAppsItem leftOrRightApps : mLeftOrRightAppsItem) {
                                        if (leftOrRightApps.getLeftApps().getNo().equals(leftApp.getNo())) {
                                            isLike = true;
                                            break;
                                        }
                                    }
                                    if (!isLike) {
                                        LeftOrRightAppsItem leftOrRightAppsItem = new LeftOrRightAppsItem(leftApp, rightApp);
                                        mLeftOrRightAppsItem.add(leftOrRightAppsItem);
                                    }
                                }
                                if (state != state_null) {
                                    AppInfo leftAppInfo, rightAppInfo;
                                    if (leftApp.getRatings_count() > 0) {
                                        leftAppInfo = new AppInfo(leftApp.getNo(), leftApp.getInstalled_times(), (leftApp.getRatings_sum() / leftApp.getRatings_count()),0);
                                    } else {
                                        leftAppInfo = new AppInfo(leftApp.getNo(), leftApp.getInstalled_times(), 0,0);
                                    }
                                    if (rightApp.getRatings_count() > 0) {
                                        rightAppInfo = new AppInfo(rightApp.getNo(), rightApp.getInstalled_times(), (rightApp.getRatings_sum() / rightApp.getRatings_count()),0);
                                    } else {
                                        rightAppInfo = new AppInfo(rightApp.getNo(), rightApp.getInstalled_times(), 0,0);
                                    }
                                    if (mDataBaseOpenHelper.QueryBeingApp(leftApp.getNo())) {//判断数据库中是否已存在
                                        mDataBaseOpenHelper.UpdateApp(leftAppInfo);//更新
                                    } else {
                                        mDataBaseOpenHelper.AddApp(leftAppInfo);//添加
                                    }

                                    if (mDataBaseOpenHelper.QueryBeingApp(rightApp.getNo())) {//判断数据库中是否已存在
                                        mDataBaseOpenHelper.UpdateApp(rightAppInfo);//更新
                                    } else {
                                        mDataBaseOpenHelper.AddApp(rightAppInfo);//添加
                                    }
                                }
                                if (ApplicationUtils.getAppMap()!=null&&ApplicationUtils.getAppMap().get("package:" + leftApp.getPackage_name()) == null) {
                                    ApplicationUtils.setAppMap("package:" + leftApp.getPackage_name(), leftApp);
                                }
                                if (ApplicationUtils.getAppMap()!=null&&ApplicationUtils.getAppMap().get("package:" + rightApp.getPackage_name()) == null) {
                                    ApplicationUtils.setAppMap("package:" + rightApp.getPackage_name(), rightApp);
                                }
                            } else {
                                Apps leftApp = item.getData().getApps()[i];

                                if (leftApp != null) {
                                    boolean isLike = false;
                                    for (LeftOrRightAppsItem leftOrRightApps : mLeftOrRightAppsItem) {
                                        if (leftOrRightApps.getLeftApps().getNo().equals(leftApp.getNo())) {
                                            isLike = true;
                                            break;
                                        }
                                    }
                                    if (!isLike) {
                                        LeftOrRightAppsItem leftOrRightAppsItem = new LeftOrRightAppsItem(leftApp, null);
                                        mLeftOrRightAppsItem.add(leftOrRightAppsItem);
                                    }
                                }
                                if (state != state_null) {
                                    AppInfo leftAppInfo;
                                    if (leftApp.getRatings_count() > 0) {
                                        leftAppInfo = new AppInfo(leftApp.getNo(), leftApp.getInstalled_times(), (leftApp.getRatings_sum() / leftApp.getRatings_count()),0);
                                    } else {
                                        leftAppInfo = new AppInfo(leftApp.getNo(), leftApp.getInstalled_times(), 0,0);
                                    }
                                    if (mDataBaseOpenHelper.QueryBeingApp(leftApp.getNo())) {//判断数据库中是否已存在
                                        mDataBaseOpenHelper.UpdateApp(leftAppInfo);//更新
                                    } else {
                                        mDataBaseOpenHelper.AddApp(leftAppInfo);//添加
                                    }
                                }
                                if (ApplicationUtils.getAppMap()!=null&&ApplicationUtils.getAppMap().get("package:" + leftApp.getPackage_name()) == null) {
                                    ApplicationUtils.setAppMap("package:" + leftApp.getPackage_name(), leftApp);
                                }
                            }
                        }
                        mAppsAdapter.setList(mLeftOrRightAppsItem);
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
            setView(false,1);
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_exceptions)+e.toString());
            }
            if (pageNum > 1) {
                pageNum--;
            }
            e.printStackTrace();
        }
    }

    @Override
    public void setFailedResponse(String value) {
        setView(false,1);
        finish(state);
        if(pageNum>1){
            pageNum--;
        }
        if(isAdded()) {
            ToastUtils.showToast(getContext(), getString(R.string.network_exceptions_again)+value);
        }
    }

    @Override
    public void setTimeoutResponse(String value) {
        if(num<3){
            startTimer();
        }else{
            setView(false,1);
            finish(state);
            if(pageNum>1){
                pageNum--;
            }
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_fail_again)+value);
            }
        }
    }

    private void finish(int state){
        num=0;
        if(state==state_load){
            // 结束加载更多
            fragment_Apps_Pulltorefreshlayout.finishLoadMore();
        }else if(state==state_refresh){
            // 结束刷新
            fragment_Apps_Pulltorefreshlayout.finishRefresh();
        }else if(state==state_init){
            mCustomDialog.hideDailog();
        }
    }

    private void setView(boolean result,int type){
        if(result){
            fragment_Apps_Liy.setVisibility(View.GONE);
            fragment_Apps_Pulltorefreshlayout.setVisibility(View.VISIBLE);
        }else{
            fragment_Apps_Liy.setVisibility(View.VISIBLE);
            fragment_Apps_Pulltorefreshlayout.setVisibility(View.GONE);
            if(type==1){
                fragment_Apps_Txt.setText(R.string.shopkeeper_busy_system_tired);
            }else if(type==2){
                fragment_Apps_Txt.setText(R.string.no_application_under_classification);
            }
        }
    }

}
