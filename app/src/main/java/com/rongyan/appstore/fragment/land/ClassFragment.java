package com.rongyan.appstore.fragment.land;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rongyan.appstore.adapter.ClassAdapter;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.database.DatabaseColume;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.CategoriesItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;;

/**
 * 横屏左侧分类列表
 */

public class ClassFragment extends Fragment implements HttpGetUtils.CallBack{

    private final static String TAG="ClassFragment";

    private RecyclerView fragment_Class_Recyclerview;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private ClassAdapter mClassAdapter=null;

    private Timer mCategoriesTimer;

    private HttpGetUtils mCategoriesUtils;

    private Handler mHandler = new Handler();

    private int num=0;

    private List<CategoriesItem.Data.Categories> mCategoies;

    private selectFragment mCallback;//定义一个接口变量

    private String lastValue="";

    /**
     * 定义一个接口
     */
    public interface selectFragment{
        void showFragment(int id);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_class, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onResume(){
        super.onResume();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity != null) {
            mCallback = (selectFragment) activity;
        }
    }

    public void init(){
        initView();
        initData();
    }

    public void initView(){
        fragment_Class_Recyclerview = (RecyclerView) getView().findViewById(R.id.fragment_class_recyclerview);
    }

    public void initEvent(){
        mClassAdapter.setOnItemClickListener(new ClassAdapter.OnItemClickListener() {
            @Override
            public void onItemClickListener(View view, int position) {
                if(mCategoies!=null&&!mCategoies.isEmpty()) {
                    mCallback.showFragment(mCategoies.get(position).getId());
                }
            }
        });
    }

    public void initData() {
        // 设置布局管理器
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getContext());
        fragment_Class_Recyclerview.setLayoutManager(linearLayoutManager);
        mCategoies=new ArrayList<>();
        judge();
    }

    public void startTimer(long time) {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            if (mCategoriesTimer != null) {
                mCategoriesTimer.cancel();
            }
            mCategoriesTimer = new Timer();
            mCategoriesTimer.schedule(new CategoriesTask(),time);
        }else{
            ToastUtils.showToast(getContext(), getString(R.string.network_failed_check_configuration));
        }
    }

    class CategoriesTask extends TimerTask {
        @Override
        public void run() {
            mCategoriesUtils = new HttpGetUtils(getContext(),ClassFragment.this, Constants.HTTP_CATEGORIES_URL, mHandler);
            mCategoriesUtils.start();
        }
    }

    @Override
    public void setResponseData(String value){
        LogUtils.w(TAG,value);
        if(!lastValue.equals(value)){
            lastValue=value;
        }else{
            return;
        }
        try {
            num=0;
            CategoriesItem item = (CategoriesItem) JsonUtils
                    .jsonToBean(value, CategoriesItem.class);
            if (item != null && item.isSuccess()) {
                if(item.getData()!=null){
                    List<CategoriesItem.Data.Categories> tmpCategories=java.util.Arrays.asList(item.getData().getCategories());
                    mCategoies = new ArrayList<>(tmpCategories);
                    CategoriesItem.Data.Categories categories=new CategoriesItem.Data.Categories();
                    categories.setId(0);
                    categories.setName(getString(R.string.homepage_recommendation));
                    categories.setIcon_url(null);
                    mCategoies.add(0,categories);
                    // 设置适配器
                    if(mClassAdapter==null) {
                        mClassAdapter = new ClassAdapter(mCategoies, getContext());
                        fragment_Class_Recyclerview.setAdapter(mClassAdapter);
                        initEvent();
                    }else{
                        mClassAdapter.setDatas(mCategoies);
                        fragment_Class_Recyclerview.setAdapter(mClassAdapter);
                        ((LinearLayoutManager)fragment_Class_Recyclerview.getLayoutManager()).scrollToPositionWithOffset(mClassAdapter.getLastPosition(), 0);
                    }
                    if (mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.CLASSID, DatabaseColume.PAGE)) {//判断数据库中是否已存在
                        mDataBaseOpenHelper.UpdateClass(DatabaseColume.CLASSID, value, DatabaseColume.PAGE);//更新
                    } else {
                        mDataBaseOpenHelper.AddClass(DatabaseColume.CLASSID, value, DatabaseColume.PAGE);//添加
                    }
                }
            }
        }catch(Exception e){
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_exceptions));
            }
            e.printStackTrace();
        }
    }

    @Override
    public void setFailedResponse(String value){
        LogUtils.w(TAG,value);
        num=0;
        if(isAdded()) {
            ToastUtils.showToast(getContext(), getString(R.string.network_exceptions_again));
        }
    }

    @Override
    public void setTimeoutResponse(String value){
        LogUtils.w(TAG,value);
        if(num<3){
            startTimer(1000);
        }else{
            num=0;
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_fail_again));
            }
        }
    }

    /**
     * 判断是否需要重新查询
     */
    public void judge() {
        Thread t = new Thread(new Runnable(){
            public void run(){
                int id=DatabaseColume.CLASSID;
                if (mDataBaseOpenHelper.QueryOverdueClass(id, 1)) {//判断是否已过期
                    mDataBaseOpenHelper.DeleteClass(id);//删除相应类别id下所有内容
                    startTimer(0);
                } else {
                    List<String> valueList = mDataBaseOpenHelper.QueryClass(id);
                    if (valueList != null && !valueList.isEmpty()) {
                        for (final String value : valueList) {
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setResponseData(value);
                                }
                            });
                        }
                    } else {
                        startTimer(0);
                    }
                }
            }});
        t.start();
    }

    public List<CategoriesItem.Data.Categories> getmCategoies() {
        return mCategoies;
    }
}
