package com.rongyan.appstore.fragment.port;

import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.rongyan.appstore.activity.port.AppDetailActivity;
import com.rongyan.appstore.adapter.AppAdapter;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.database.DatabaseColume;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.AppInfoItem;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.item.BannersItem;
import com.rongyan.appstore.item.NewestHotestItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.ToastUtils;
import com.rongyan.appstore.viewpager.CycleViewPager;
import com.rongyan.appstore.viewpager.ViewFactory;
import com.rongyan.appstore.widget.AppView;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;

/**
 * 竖屏首页Fragment
 */
public class HomePageFragment extends Fragment implements HttpGetUtils.CallBack,AppView.app{

    private final static String TAG="HomePageFragment";

    public static final int HOTEST = 1;//最热

    public static final int NEWEST =2;//最新

    private int state_null=0,state_init=1;

    public final static int type_banner=1,type_hotest=2,type_newest=3;

    private int type_temporary=1;

    private DataBaseOpenHelper mDataBaseOpenHelper=null;

    private CycleViewPager fragment_Homepage_Viewpager;

    private ViewPager fragment_Homepage_Viewpagers;

    private Button fragment_Homepage_Btn;

    private TextView fragment_Homepage_Hotest_Txt,fragment_Homepage_Newest_Txt;

    private ListView homepage_Hotest_Listview,homepage_Newest_Listview;

    private Timer mBannersTimer,mHotestTimer,mNewestTimer;

    private HttpGetUtils mBannersUtils,mHotestUtils,mNewestUtils;

    private Handler mHandler = new Handler();

    private List<ImageView> views;

    private List<BannersItem.Data.Banners> infos = new ArrayList<>();

    private AppAdapter hotestAdapter,newestAdapter;

    private CustomDialog mCustomDialog=null;

    private List<View> listViews; // Tab页面列表

    private int currentIndex;//记录上一次点击区域

    private int currIndex = 0;// 当前页卡编号(最热：0，最新：1)

    private int offset = 0;// 动画图片偏移量

    private int num=0;

    private int hotOrNew=2;

    private List<Apps> mAppsItem;

    private int state=state_init;

    private boolean isFirst=true;//是否首次进入该页面

    private boolean isMove=false;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_homepage, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init(savedInstanceState);
    }

    @Override
    public void onResume(){
        if(!isFirst){
            if(hotOrNew==type_hotest){
                hotestAdapter.notifyDataSetChanged();
            }else if(hotOrNew==type_newest){
                newestAdapter.notifyDataSetChanged();
            }
        }
        isFirst=false;
        super.onResume();
    }

    public void init(Bundle savedInstanceState){
        //configImageLoader();
        initView();
        initViewPager(savedInstanceState);
        initEvent();
        initData();
    }

    /**
     * 配置ImageLoder
     */
    private void configImageLoader() {
        // 初始化ImageLoader
        @SuppressWarnings("deprecation")
        DisplayImageOptions options = new DisplayImageOptions.Builder().showStubImage(R.drawable.banner_default) // 设置图片下载期间显示的图片
                .showImageForEmptyUri(R.drawable.banner_default) // 设置图片Uri为空或是错误的时候显示的图片
                .showImageOnFail(R.drawable.banner_default) // 设置图片加载或解码过程中发生错误显示的图片
                .cacheInMemory(true) // 设置下载的图片是否缓存在内存中
                .cacheOnDisc(true) // 设置下载的图片是否缓存在SD卡中
                .displayer(new RoundedBitmapDisplayer(10)) // 设置成圆角图片
                .build(); // 创建配置过得DisplayImageOption对象

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(options)
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator()).tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
    }

    public void initView(){
        fragment_Homepage_Viewpager=(CycleViewPager)getChildFragmentManager().findFragmentById(R.id.fragment_homepage_viewpager);
        fragment_Homepage_Viewpagers=(ViewPager)getView().findViewById(R.id.fragment_homepage_viewpagers);
        fragment_Homepage_Btn=(Button)getView().findViewById(R.id.fragment_homepage_btn);
        fragment_Homepage_Hotest_Txt=(TextView)getView().findViewById(R.id.fragment_homepage_hotest_txt);
        fragment_Homepage_Newest_Txt=(TextView)getView().findViewById(R.id.fragment_homepage_newest_txt);
    }

    public void initEvent(){
        fragment_Homepage_Hotest_Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.w(TAG,"HOTEST");
                if(toggleBtn(HOTEST)) {
                    //judgeNew_Hot(type_hotest);
                    fragment_Homepage_Viewpagers.setCurrentItem(0,false);
                }
            }
        });
        fragment_Homepage_Newest_Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LogUtils.w(TAG,"NEWEST");
                if(toggleBtn(NEWEST)) {
                    //judgeNew_Hot(type_newest);
                    fragment_Homepage_Viewpagers.setCurrentItem(1,false);
                }
            }
        });
        fragment_Homepage_Btn.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                float x1 = 0;
                float x2 = 0;
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    //当手指按下的时候
                    x1 = event.getX();
                    isMove=false;
                }
                if(event.getAction() == MotionEvent.ACTION_MOVE){
                    isMove=true;
                }
                if(event.getAction() == MotionEvent.ACTION_UP) {
                    //当手指离开的时候
                    x2 = event.getX();
                    if(x1 - x2 > 0&&isMove) {
                        if(toggleBtn(HOTEST)) {
                            fragment_Homepage_Viewpagers.setCurrentItem(0,false);
                        }
                    } else if(x2 - x1 >0&&isMove) {
                        if(toggleBtn(NEWEST)) {
                            fragment_Homepage_Viewpagers.setCurrentItem(1,false);
                        }
                    }
                }
                return false;
            }
        });
    }

    public void initData(){
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getContext());
        hotestAdapter = new AppAdapter(HomePageFragment.this, getContext());
        newestAdapter = new AppAdapter(HomePageFragment.this, getContext());
        homepage_Hotest_Listview.setAdapter(hotestAdapter);
        homepage_Newest_Listview.setAdapter(newestAdapter);
        mCustomDialog = new CustomDialog(getActivity());
        toggleBtn(HOTEST);
        if(!ApplicationUtils.getBanner_location().equals("")&&ApplicationUtils.getBanners_count()!=0){
            judgeNew_Hot(type_banner);
        }
        if(ApplicationUtils.getApps_count()!=0){
            judgeNew_Hot(getHotOrNew());
        }
    }

    private void setViewPager(){
        views = new ArrayList<>();
        // 将最后一个ImageView添加进来
        views.add(ViewFactory.getImageView(getContext(), infos.get(infos.size() - 1).getPicture_url()));
        for (int i = 0; i < infos.size(); i++) {
            views.add(ViewFactory.getImageView(getContext(), infos.get(i).getPicture_url()));
        }
        // 将第一个ImageView添加进来
        views.add(ViewFactory.getImageView(getContext(), infos.get(0).getPicture_url()));
        // 设置循环，getPicture_url
        fragment_Homepage_Viewpager.setCycle(true);

        // 在加载数据前设置是否循环
        fragment_Homepage_Viewpager.setData(views, infos, mAdCycleViewListener,getContext());
        //设置轮播
        if(views.size()<4) {
            fragment_Homepage_Viewpager.setWheel(false);
        }else{
            fragment_Homepage_Viewpager.setWheel(true);
        }

        // 设置轮播时间，默认5000ms
        fragment_Homepage_Viewpager.setTime(2000);
        //设置圆点指示图标组居中显示，默认靠右
        fragment_Homepage_Viewpager.setIndicatorCenter();
    }

    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = new CycleViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(BannersItem.Data.Banners info, int position, View imageView) {
            if (fragment_Homepage_Viewpager.isCycle()) {
                position = position - 1;
                if(infos!=null&&!infos.isEmpty()){
                    try {
                        BannersItem.Data.Banners mInfo = infos.get(position);
                        if (mInfo.getBehavior() == 0) {//跳转app
                            boolean isFind=false;
                            List<AppInfoItem> appInfoList=ApplicationUtils.getAppInfos();
                            if(appInfoList!=null&&!appInfoList.isEmpty()){
                                for(AppInfoItem appInfo:appInfoList){
                                    if(appInfo.getAppPackageName().equals(mInfo.getApp().getPackage_name())){
                                        isFind=true;
                                        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(appInfo.getAppPackageName());
                                        startActivity(intent);
                                        break;
                                    }
                                }
                            }
                            if(!isFind) {
                                getAppNo(mInfo.getApp().getNo());
                            }
                        } else if (mInfo.getBehavior() == 1) {//跳转url
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(mInfo.getUrl());
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }

            }
        }

    };

    /**
     * 切换最新最热列表
     */
    public boolean toggleBtn(int index) {
        if(currentIndex==index){
            return false;
        }
        setBtnView(index);
        currentIndex=index;
        return true;
    }

    private void setBtnView(int index){
        if(index==HOTEST){
            hotOrNew=type_hotest;
            ObjectAnimator.ofFloat(fragment_Homepage_Btn, "translationX", 0, -120).setDuration(00).start();
            fragment_Homepage_Btn.setText(getString(R.string.hotest));
            fragment_Homepage_Viewpagers.setCurrentItem(0);
        }else{
            hotOrNew=type_newest;
            ObjectAnimator.ofFloat(fragment_Homepage_Btn, "translationX", 0, 120).setDuration(00).start();
            fragment_Homepage_Btn.setText(getString(R.string.newest));
            fragment_Homepage_Viewpagers.setCurrentItem(1);
        }
    }

    /**
     * 初始化ViewPager
     */
    private void initViewPager(Bundle savedInstanceState) {
        listViews = new ArrayList();
        @SuppressLint("RestrictedApi")
        LayoutInflater mInflater = LayoutInflater.from(getContext());
        View hotestView = mInflater.inflate(R.layout.homepage_hotest, null);
        View newestView = mInflater.inflate(R.layout.homepage_newest, null);
        listViews.add(hotestView);
        listViews.add(newestView);
        fragment_Homepage_Viewpagers.setAdapter(new MyPagerAdapter(listViews));
        fragment_Homepage_Viewpagers.setCurrentItem(0, false);
        fragment_Homepage_Viewpagers.addOnPageChangeListener(new MyOnPageChangeListener());
        initPageView(hotestView,newestView);
    }

    public void initPageView(View hotestView,View newestView){
        homepage_Hotest_Listview=(ListView)hotestView.findViewById(R.id.homepage_hotest_listview);
        homepage_Newest_Listview=(ListView)newestView.findViewById(R.id.homepage_newest_listview);
    }

    /**
     * ViewPager适配器
     */
    public static class MyPagerAdapter extends PagerAdapter {
        public List<View> mListViews;
        public MyPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }
        @Override
        public void destroyItem(View arg0, int arg1, Object arg2) {
            ((ViewPager) arg0).removeView(mListViews.get(arg1));
        }
        @Override
        public void finishUpdate(View arg0) {
        }
        @Override
        public int getCount() {
            return mListViews.size();
        }
        @Override
        public Object instantiateItem(View arg0, int arg1) {
            ((ViewPager) arg0).addView(mListViews.get(arg1), 0);
            return mListViews.get(arg1);
        }
        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == (arg1);
        }
        @Override
        public void restoreState(Parcelable arg0, ClassLoader arg1) {
        }
        @Override
        public Parcelable saveState() {
            return null;
        }
        @Override
        public void startUpdate(View arg0) {
        }
    }

    /**
     * 页卡切换监听
     */
    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        int one = offset * 2 ;// 页卡1 -> 页卡2 偏移量
        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                case 0:
                    if (currIndex == 1) {
                        animation = new TranslateAnimation(one, 0, 0, 0);
                    }
                    break;
                case 1:
                    if (currIndex == 0) {
                        animation = new TranslateAnimation(offset, one, 0, 0);
                    }
                    break;
                default:
                    animation = new TranslateAnimation(offset, one, 0, 0);
                    break;
            }
            currIndex = arg0;
            LogUtils.w(TAG,"currIndex:"+currIndex);
            if(fragment_Homepage_Viewpagers!=null) {
                fragment_Homepage_Viewpagers.setCurrentItem(currIndex);
            }
            toggleBtn(currIndex+1);
            if(animation!=null) {
                animation.setFillAfter(true);// True:图片停在动画结束位置
                animation.setDuration(300);
            }
            if(currIndex==HOTEST){
                judgeNew_Hot(type_newest);
            }else{
                judgeNew_Hot(type_hotest);
            }
        }
        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }
        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    public void startTimer(int type) {
        if (ApplicationUtils.ismNetWorkEnable()){
            if(type_temporary!=type){
                num=0;
                type_temporary=type;
            }else{
                num++;
            }
            state=state_init;
            if(isAdded()) {
                mCustomDialog.showDailog();
            }
            if (type==type_banner) {
                if(ApplicationUtils.getBanners_count()!=0) {
                    if (mBannersTimer != null) {
                        mBannersTimer.cancel();
                    }
                    mBannersTimer = new Timer();
                    mBannersTimer.schedule(new BannersTask(ApplicationUtils.getBanner_location(), ApplicationUtils.getBanners_count()), 0);
                }
            }else if(type==type_hotest){
                if (mHotestTimer != null) {
                    mHotestTimer.cancel();
                }
                mHotestTimer = new Timer();
                mHotestTimer.schedule(new HotestTask(), 0);
            }else if(type==type_newest){
                if (mNewestTimer != null) {
                    mNewestTimer.cancel();
                }
                mNewestTimer = new Timer();
                mNewestTimer.schedule(new NewestTask(), 0);
            }
        }else{
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_failed_check_configuration));
            }
        }
    }

    class BannersTask extends TimerTask {
        private String mLocation;

        private int mCount;

        BannersTask(String location,int count){
            this.mLocation=location;
            this.mCount=count;
        }
        @Override
        public void run() {
            mBannersUtils = new HttpGetUtils(getContext(),HomePageFragment.this, Constants.HTTP_BANNERS_URL+mLocation+"&count="+mCount, mHandler);
            mBannersUtils.start();
        }
    }

    class HotestTask extends TimerTask {
        @Override
        public void run() {
            mHotestUtils = new HttpGetUtils(getContext(),HomePageFragment.this, Constants.HTTP_HOTEST_URL+ ApplicationUtils.getApps_count(), mHandler);
            mHotestUtils.start();
        }
    }

    class NewestTask extends TimerTask {
        @Override
        public void run() {
            mNewestUtils = new HttpGetUtils(getContext(),HomePageFragment.this, Constants.HTTP_NEWEST_URL+ApplicationUtils.getApps_count(), mHandler);
            mNewestUtils.start();
        }
    }

    @Override
    public void setResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            finish(state);
            if (value.indexOf("banners") == -1) {
                mAppsItem=new ArrayList<>();
                NewestHotestItem item = (NewestHotestItem) JsonUtils
                        .jsonToBean(value, NewestHotestItem.class);
                if (item != null && item.isSuccess()) {
                    if (item.getData() != null) {
                        if(item.getData().getApps()!=null&&item.getData().getApps().length>0){
                            if(hotOrNew==type_hotest){
                                if(mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.HOTID,DatabaseColume.PAGE) ){//判断数据库中是否已存在
                                    mDataBaseOpenHelper.UpdateClass(DatabaseColume.HOTID, value,DatabaseColume.PAGE);//更新
                                }else{
                                    mDataBaseOpenHelper.AddClass(DatabaseColume.HOTID, value,DatabaseColume.PAGE);//添加
                                }
                            }else{
                                if(mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.NEWID,DatabaseColume.PAGE) ){//判断数据库中是否已存在
                                    mDataBaseOpenHelper.UpdateClass(DatabaseColume.NEWID, value,DatabaseColume.PAGE);//更新
                                }else{
                                    mDataBaseOpenHelper.AddClass(DatabaseColume.NEWID, value,DatabaseColume.PAGE);//添加
                                }
                            }
                            for (int i = 0; i < item.getData().getApps().length; i++) {
                                Apps app = item.getData().getApps()[i];
                                if (app != null) {
                                    mAppsItem.add(app);
                                    if (ApplicationUtils.getAppMap().get("package:" + app.getPackage_name()) == null) {
                                        ApplicationUtils.setAppMap("package:" + app.getPackage_name(), app);
                                    }
                                    if(state==state_init) {
                                        AppInfo appInfo;
                                        if(app.getRatings_count()>0) {
                                            appInfo = new AppInfo(app.getNo(), app.getInstalled_times(), (app.getRatings_sum() / app.getRatings_count()),0);
                                        }else{
                                            appInfo = new AppInfo(app.getNo(), app.getInstalled_times(),0,0);
                                        }
                                        if (mDataBaseOpenHelper.QueryBeingApp(app.getNo())) {//判断数据库中是否已存在
                                            mDataBaseOpenHelper.UpdateApp(appInfo);//更新
                                        } else {
                                            mDataBaseOpenHelper.AddApp(appInfo);//添加
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                if(hotOrNew==type_hotest) {
                    hotestAdapter.setList(mAppsItem);
                }else{
                    newestAdapter.setList(mAppsItem);
                }
            }else{
                BannersItem item = (BannersItem) JsonUtils
                        .jsonToBean(value, BannersItem.class);
                if (item != null && item.isSuccess()) {
                    if (item.getData() != null) {
                        infos= java.util.Arrays.asList(item.getData().getBanners());
                        if(infos!=null&&!infos.isEmpty()) {
                            setViewPager();
                        }
                        if(mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.BANNERID,DatabaseColume.PAGE) ){//判断数据库中是否已存在
                            mDataBaseOpenHelper.UpdateClass(DatabaseColume.BANNERID, value,DatabaseColume.PAGE);//更新
                        }else{
                            mDataBaseOpenHelper.AddClass(DatabaseColume.BANNERID, value,DatabaseColume.PAGE);//添加
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_exceptions)+e.toString());
            }
        }
    }

    @Override
    public void setFailedResponse(String value) {
        finish(state);
    }

    @Override
    public void setTimeoutResponse(String value) {
        if(num<3){
            startTimer(type_temporary);
        }else{
            finish(state);
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_fail_again)+value);
            }
        }
    }

    @Override
    public void getAppNo(String app_no) {
        Intent intent = new Intent(getActivity(), AppDetailActivity.class);
        intent.putExtra("app_no",app_no);
        startActivity(intent);
    }

    public int getHotOrNew() {
        return hotOrNew;
    }

    /**
     * 判断是否需要重新查询
     */
    public void judgeNew_Hot(int type) {
        LogUtils.w(TAG,"type:"+type);
        int id;
        if (type == type_hotest) {
            id = DatabaseColume.HOTID;
        } else if(type == type_newest){
            id = DatabaseColume.NEWID;
        } else{
            id=  DatabaseColume.BANNERID;
        }
        if (mDataBaseOpenHelper.QueryOverdueClass(id, 1)) {//判断是否已过期
            mDataBaseOpenHelper.DeleteClass(id);//删除相应类别id下所有内容
            startTimer(type);//重新请求
        } else {
            List<String> valueList = mDataBaseOpenHelper.QueryClass(id);
            if (valueList != null && !valueList.isEmpty()) {
                for (String value : valueList) {
                    state=state_null;
                    setResponseData(value);
                }
            } else {
                startTimer(type);
            }
        }

    }

    private void finish(int state){
        num=0;
        if(state==state_init){
            mCustomDialog.hideDailog();
        }
    }

    public void initInterface(){
        List<String> valueList = mDataBaseOpenHelper.QueryClass( DatabaseColume.HOTID);
        if (valueList != null && !valueList.isEmpty()) {
            for (String value : valueList) {
                state=state_null;
                setResponseData(value);
            }
        }
        List<String> valueList1 = mDataBaseOpenHelper.QueryClass( DatabaseColume.BANNERID);
        if (valueList1 != null && !valueList1.isEmpty()) {
            for (String value : valueList1) {
                state=state_null;
                setResponseData(value);
            }
        }
    }

}
