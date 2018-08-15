package com.rongyan.appstore.fragment.land;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.nostra13.universalimageloader.cache.disc.naming.Md5FileNameGenerator;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;
import com.nostra13.universalimageloader.core.display.SimpleBitmapDisplayer;
import com.rongyan.appstore.adapter.RecommendAdapter;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.database.DatabaseColume;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.AppInfoItem;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.item.BannersItem;
import com.rongyan.appstore.item.LeftOrRightAppsItem;
import com.rongyan.appstore.item.NewestHotestItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.DensityUtils;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.ToastUtils;
import com.rongyan.appstore.viewpager.CycleViewPager;
import com.rongyan.appstore.viewpager.ViewFactory;
import com.rongyan.appstore.widget.AppView;
import com.zhy.fabridge.lib.Fabridge;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;

/**
 * 横屏首页推荐
 */

public class RecommendFragment extends Fragment implements HttpGetUtils.CallBack,AppView.app {

    private final static String TAG = "RecommendFragment";

    private int state_null = 0, state_init = 1;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private CycleViewPager fragment_Recommend_Viewpager;

    private CustomDialog mCustomDialog;

    private FrameLayout fragment_Recommend_Liy;

    private LinearLayout fragment_Recommend_Search_Liy;

    private Button fragment_Recommend_Btn;

    private TextView fragment_Recommend_Newest_Txt, fragment_Recommend_Hotest_Txt;

    private EditText fragment_Recommend_Search_Edit;

    private ListView fragment_Recommend_Listview;

    private RecommendAdapter mRecommendAdapter;

    private ImageView fragment_Recommend_Img;

    private Timer mBannersTimer, mHotestTimer, mNewestTimer;

    private HttpGetUtils mBannersUtils, mHotestUtils, mNewestUtils;

    private Handler mHandler = new Handler();

    private List<ImageView> views;

    private List<BannersItem.Data.Banners> infos = new ArrayList<>();

    private List<LeftOrRightAppsItem> mLeftOrRightAppsItem;

    public final static int type_banner = 1, type_hotest = 2, type_newest = 3;

    private int type_temporary = 1;

    private int hotOrNew = 2;

    private int num = 0;

    private int state = state_init;

    private boolean isFirst = true;//是否首次进入该页面

    private int currentIndex;//记录上一次点击区域
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recommend, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onResume() {
        if (!isFirst) {
            mRecommendAdapter.mCount = 0;
            mRecommendAdapter.notifyDataSetChanged();
        }
        isFirst = false;
        super.onResume();
    }

    public void init() {
        //configImageLoader();
        initView();
        initEvent();
        initData();
    }

    public void initView() {
        fragment_Recommend_Viewpager = (CycleViewPager) getChildFragmentManager().findFragmentById(R.id.fragment_recommend_viewpager);
        fragment_Recommend_Liy = (FrameLayout) getView().findViewById(R.id.fragment_recommend_liy);
        fragment_Recommend_Btn=(Button)getView().findViewById(R.id.fragment_recommend_btn);
        fragment_Recommend_Hotest_Txt=(TextView)getView().findViewById(R.id.fragment_recommend_hotest_txt);
        fragment_Recommend_Newest_Txt=(TextView)getView().findViewById(R.id.fragment_recommend_newest_txt);
        fragment_Recommend_Listview = (ListView) getView().findViewById(R.id.fragment_recommend_listview);
        fragment_Recommend_Search_Liy = (LinearLayout) getView().findViewById(R.id.fragment_recommend_search_liy);
        fragment_Recommend_Search_Edit = (EditText) getView().findViewById(R.id.fragment_recommend_search_edit);
        fragment_Recommend_Img = (ImageView) getView().findViewById(R.id.fragment_recommend_img);
        toggleBtn(type_hotest);
    }

    public void initEvent() {
        fragment_Recommend_Hotest_Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (hotOrNew == type_newest) {
                    hotOrNew = type_hotest;
                    toggleBtn(type_hotest);
                    judgeNew_Hot(type_hotest);
                }
            }
        });
        fragment_Recommend_Newest_Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(hotOrNew == type_hotest) {
                    hotOrNew = type_newest;
                    toggleBtn(type_newest);
                    judgeNew_Hot(type_newest);
                }
            }
        });
        fragment_Recommend_Search_Edit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入的内容变化的监听
                if(s.toString().equals("")){
                    fragment_Recommend_Search_Liy.setVisibility(View.VISIBLE);
                }else{
                    fragment_Recommend_Search_Liy.setVisibility(View.GONE);
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
        fragment_Recommend_Search_Edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                fragment_Recommend_Search_Edit.setFocusable(true);
                fragment_Recommend_Search_Edit.setFocusableInTouchMode(true);
                return false;
            }
        });

    }

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
        if(index==type_hotest){
            hotOrNew=type_hotest;
            ObjectAnimator.ofFloat(fragment_Recommend_Btn, "translationX", 0, DensityUtils.dip2px(getContext(),-95)).setDuration(00).start();
            fragment_Recommend_Btn.setText(getString(R.string.hotest));
        }else{
            hotOrNew=type_newest;
            ObjectAnimator.ofFloat(fragment_Recommend_Btn, "translationX", 0, DensityUtils.dip2px(getContext(),95)).setDuration(00).start();
            fragment_Recommend_Btn.setText(getString(R.string.newest));
        }
    }

    public void initData() {
        mCustomDialog = new CustomDialog(getActivity());
        mRecommendAdapter = new RecommendAdapter(this, getContext());
        fragment_Recommend_Listview.setAdapter(mRecommendAdapter);
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getContext());
        if(!ApplicationUtils.getBanner_location().equals("")&&ApplicationUtils.getBanners_count()!=0){
            judgeNew_Hot(type_banner);
        }
        if(ApplicationUtils.getApps_count()!=0){
            judgeNew_Hot(getHotOrNew());
        }
    }

    private void setViewPager() {
        views = new ArrayList<>();
        // 将最后一个ImageView添加进来
        views.add(ViewFactory.getImageView(getContext(), infos.get(infos.size() - 1).getPicture_url()));
        for (int i = 0; i < infos.size(); i++) {
            views.add(ViewFactory.getImageView(getContext(), infos.get(i).getPicture_url()));
        }
        // 将第一个ImageView添加进来
        views.add(ViewFactory.getImageView(getContext(), infos.get(0).getPicture_url()));
        // 设置循环，getPicture_url
        fragment_Recommend_Viewpager.setCycle(true);

        // 在加载数据前设置是否循环
        fragment_Recommend_Viewpager.setData(views, infos, mAdCycleViewListener, getContext());
        //设置轮播
        if(views.size()<4) {
            fragment_Recommend_Viewpager.setWheel(false);
        }else{
            fragment_Recommend_Viewpager.setWheel(true);
        }

        // 设置轮播时间，默认5000ms
        fragment_Recommend_Viewpager.setTime(2000);
        //设置圆点指示图标组居中显示，默认靠右
        fragment_Recommend_Viewpager.setIndicatorCenter();
    }

    public void startTimer(int type) {
        if (ApplicationUtils.ismNetWorkEnable()) {
            state = state_init;
            if(isAdded()) {
                mCustomDialog.showDailog();
            }
            if (type_temporary != type) {
                num = 0;
                type_temporary = type;
            } else {
                num++;
            }
            if (type == type_banner) {
                if (ApplicationUtils.getBanners_count() != 0) {
                    if (mBannersTimer != null) {
                        mBannersTimer.cancel();
                    }
                    mBannersTimer = new Timer();
                    mBannersTimer.schedule(new BannersTask(ApplicationUtils.getBanner_location(), ApplicationUtils.getBanners_count()), 0);
                }
            } else if (type == type_hotest) {
                if (mHotestTimer != null) {
                    mHotestTimer.cancel();
                }
                mHotestTimer = new Timer();
                mHotestTimer.schedule(new HotestTask(), 0);
            } else if (type == type_newest) {
                if (mNewestTimer != null) {
                    mNewestTimer.cancel();
                }
                mNewestTimer = new Timer();
                mNewestTimer.schedule(new NewestTask(), 0);
            }
        } else {
            ToastUtils.showToast(getContext(), getString(R.string.network_failed_check_configuration));
        }
    }

    @Override
    public void getAppNo(String app_no) {
        Fabridge.call(getActivity(), "APP_ITEM_ID", app_no);  //调用ID对应的方法
    }

    class BannersTask extends TimerTask {
        private String mLocation;

        private int mCount;

        BannersTask(String location, int count) {
            this.mLocation = location;
            this.mCount = count;
        }

        @Override
        public void run() {
            mBannersUtils = new HttpGetUtils(getContext(), RecommendFragment.this, Constants.HTTP_BANNERS_URL + mLocation + "&count=" + mCount, mHandler);
            mBannersUtils.start();
        }
    }

    class HotestTask extends TimerTask {
        @Override
        public void run() {
            mHotestUtils = new HttpGetUtils(getContext(), RecommendFragment.this, Constants.HTTP_HOTEST_URL + ApplicationUtils.getApps_count(), mHandler);
            mHotestUtils.start();
        }
    }

    class NewestTask extends TimerTask {
        @Override
        public void run() {
            mNewestUtils = new HttpGetUtils(getContext(), RecommendFragment.this, Constants.HTTP_NEWEST_URL + ApplicationUtils.getApps_count(), mHandler);
            mNewestUtils.start();
        }
    }

    @Override
    public void setResponseData(String value) {
        LogUtils.w(TAG, value);
        try {
            finish(state);
            if (value.indexOf("banners") == -1) {
                mLeftOrRightAppsItem = new ArrayList<>();
                NewestHotestItem item = (NewestHotestItem) JsonUtils
                        .jsonToBean(value, NewestHotestItem.class);
                if (item != null && item.isSuccess()) {
                    if (item.getData() != null) {
                        if (item.getData().getApps() != null && item.getData().getApps().length > 0) {
                            if (hotOrNew == type_hotest) {
                                if (mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.HOTID, DatabaseColume.PAGE)) {//判断数据库中是否已存在
                                    mDataBaseOpenHelper.UpdateClass(DatabaseColume.HOTID, value, DatabaseColume.PAGE);//更新
                                } else {
                                    mDataBaseOpenHelper.AddClass(DatabaseColume.HOTID, value, DatabaseColume.PAGE);//添加
                                }
                            } else {
                                if (mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.NEWID, DatabaseColume.PAGE)) {//判断数据库中是否已存在
                                    mDataBaseOpenHelper.UpdateClass(DatabaseColume.NEWID, value, DatabaseColume.PAGE);//更新
                                } else {
                                    mDataBaseOpenHelper.AddClass(DatabaseColume.NEWID, value, DatabaseColume.PAGE);//添加
                                }
                            }

                            for (int i = 0; i < item.getData().getApps().length; i = i + 2) {
                                if (i + 1 < item.getData().getApps().length) {
                                    Apps leftApp = item.getData().getApps()[i];
                                    Apps rightApp = item.getData().getApps()[i + 1];
                                    if (leftApp != null && rightApp != null) {
                                        LeftOrRightAppsItem leftOrRightAppsItem = new LeftOrRightAppsItem(leftApp, rightApp);
                                        mLeftOrRightAppsItem.add(leftOrRightAppsItem);
                                    }
                                    if (state == state_init) {
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
                                        LeftOrRightAppsItem leftOrRightAppsItem = new LeftOrRightAppsItem(leftApp, null);
                                        mLeftOrRightAppsItem.add(leftOrRightAppsItem);
                                    }
                                    if (state == state_init) {
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
                            setListViewHeightBasedOnChildren(fragment_Recommend_Listview,item.getData().getApps().length);//解决ScrollView嵌套ListView只显示一行的bug
                            mRecommendAdapter.setList(mLeftOrRightAppsItem);
                        }
                    }
                }
            } else {
                BannersItem item = (BannersItem) JsonUtils
                        .jsonToBean(value, BannersItem.class);
                if (item != null && item.isSuccess()) {
                    if (item.getData() != null) {
                        infos = java.util.Arrays.asList(item.getData().getBanners());
                        if (infos != null && !infos.isEmpty()) {
                            fragment_Recommend_Img.setVisibility(View.GONE);
                            setViewPager();
                        }else{
                            fragment_Recommend_Img.setVisibility(View.VISIBLE);
                        }
                        if (mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.BANNERID, DatabaseColume.PAGE)) {//判断数据库中是否已存在
                            mDataBaseOpenHelper.UpdateClass(DatabaseColume.BANNERID, value, DatabaseColume.PAGE);//更新
                        } else {
                            mDataBaseOpenHelper.AddClass(DatabaseColume.BANNERID, value, DatabaseColume.PAGE);//添加
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_exceptions)+toString());
            }
        }
    }

    @Override
    public void setFailedResponse(String value) {
        finish(state);
        if(isAdded()) {
            ToastUtils.showToast(getContext(), getString(R.string.network_exceptions_again)+value);
        }
    }

    @Override
    public void setTimeoutResponse(String value) {
        if (num < 3) {
            if (type_temporary == type_banner) {
                startTimer(type_banner);
            } else if (type_temporary == type_hotest) {
                startTimer(type_hotest);
            } else if (type_temporary == type_newest) {
                startTimer(type_newest);
            }
        } else {
            finish(state);
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_fail_again)+value);
            }
        }
    }

    private CycleViewPager.ImageCycleViewListener mAdCycleViewListener = new CycleViewPager.ImageCycleViewListener() {

        @Override
        public void onImageClick(BannersItem.Data.Banners info, int position, View imageView) {
            if (fragment_Recommend_Viewpager.isCycle()) {
                position = position - 1;
                if (infos != null && !infos.isEmpty()) {
                    try {
                        BannersItem.Data.Banners mInfo = infos.get(position);
                        if (mInfo.getBehavior() == 0) {//跳转app
                            boolean isFind = false;
                            List<AppInfoItem> appInfoList = ApplicationUtils.getAppInfos();
                            if (appInfoList != null && !appInfoList.isEmpty()) {
                                for (AppInfoItem appInfo : appInfoList) {
                                    if (appInfo.getAppPackageName().equals(mInfo.getApp().getPackage_name())) {
                                        isFind = true;
                                        Intent intent = getContext().getPackageManager().getLaunchIntentForPackage(appInfo.getAppPackageName());
                                        startActivity(intent);
                                        break;
                                    }
                                }
                            }
                            if (!isFind) {
                                getAppNo(mInfo.getApp().getNo());
                            }
                        } else if (mInfo.getBehavior() == 1) {//跳转url
                            Intent intent = new Intent();
                            intent.setAction("android.intent.action.VIEW");
                            Uri content_url = Uri.parse(mInfo.getUrl());
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }
        }

    };

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
                .imageScaleType(ImageScaleType.IN_SAMPLE_INT)
                .displayer(new SimpleBitmapDisplayer())
                .build(); // 创建配置过得DisplayImageOption对象

        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getContext()).defaultDisplayImageOptions(options)
                .threadPriority(Thread.NORM_PRIORITY - 2).denyCacheImageMultipleSizesInMemory()
                .discCacheFileNameGenerator(new Md5FileNameGenerator())
                .tasksProcessingOrder(QueueProcessingType.LIFO).build();
        ImageLoader.getInstance().init(config);
    }

    public int getHotOrNew() {
        return hotOrNew;
    }

    /**
     * 判断是否需要重新查询
     */
    public void judgeNew_Hot(final int type) {
        Thread t = new Thread(new Runnable(){
            public void run(){
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
                    startTimer(type);
                } else {
                    List<String> valueList = mDataBaseOpenHelper.QueryClass(id);
                    if (valueList != null && !valueList.isEmpty()) {
                        state = state_null;
                        for (final String value : valueList) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setResponseData(value);
                                }
                            });
                        }
                    } else {
                        startTimer(type);
                    }
                }
            }});
        t.start();
    }

    /**
     * 计算listview高度
     */
    public void setListViewHeightBasedOnChildren(ListView listView,int num) {
        int len;
        if(num%2!=0){
            len=num/2+1;
        }else{
            len=num/2;
        }
        // 获取ListView对应的Adapter
        ListAdapter listAdapter = listView.getAdapter();
        if (listAdapter == null) {
            return;
        }

        int totalHeight = 0;
        for (int i = 0; i < len; i++) {
            // listAdapter.getCount()返回数据项的数目
            View listItem = listAdapter.getView(i, null, listView);
            // 计算子项View 的宽高
            listItem.measure(0, 0);
            // 统计所有子项的总高度
            totalHeight += listItem.getMeasuredHeight();
        }

        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + (listView.getDividerHeight() * (listAdapter.getCount() - 1));
        // listView.getDividerHeight()获取子项间分隔符占用的高度
        // params.height最后得到整个ListView完整显示需要的高度
        listView.setLayoutParams(params);
    }

    private void finish(int state) {
        num = 0;
        if (state == state_init) {
            mCustomDialog.hideDailog();
        }
    }

    public String getSearch(){
        return fragment_Recommend_Search_Edit.getText().toString();
    }

    public EditText getFragment_Recommend_Search_Edit() {
        return fragment_Recommend_Search_Edit;
    }

}
