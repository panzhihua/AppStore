package com.rongyan.appstore.fragment.port;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.rongyan.appstore.activity.port.AppListActivity;
import com.rongyan.appstore.adapter.ClassificationAdapter;
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

import com.rongyan.appstore.R;

/**
 * 竖屏分类Fragment
 */

public class ClassificationFragment extends Fragment implements HttpGetUtils.CallBack{

    private final static String TAG="ClassificationFragment";

    private ListView fragment_Classification_Listview;

    private ClassificationAdapter mClassificationAdapter;

    private Timer mCategoriesTimer;

    private HttpGetUtils mCategoriesUtils;

    private CustomDialog mCustomDialog;

    private Handler mHandler = new Handler();

    private List<CategoriesItem.Data.Categories> mCategoies;

    private int num=0;//请求次数

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_classification, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onResume(){
        LogUtils.w(TAG,"onResume");
        super.onResume();
    }

    public void init(){
        initView();
        initEvent();
        initData();
    }

    public void initView(){
        fragment_Classification_Listview=(ListView)getView().findViewById(R.id.fragment_classification_listview);
    }

    public void initEvent(){
        fragment_Classification_Listview.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                CategoriesItem.Data.Categories mCategories=mCategoies.get(position);
                if(mCategories!=null){
                    Intent intent = new Intent(getContext(), AppListActivity.class);
                    intent.putExtra("id",mCategories.getId());
                    intent.putExtra("name",mCategories.getName());
                    startActivity(intent);
                }
            }
        });
    }

    public void initData(){
        mClassificationAdapter=new ClassificationAdapter(getContext());
        fragment_Classification_Listview.setAdapter(mClassificationAdapter);
        if(mCustomDialog==null) {
            mCustomDialog = new CustomDialog(getActivity());
        }
        startTimer(0);
    }

    public void startTimer(long time) {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            if(isAdded()) {
                mCustomDialog.showDailog();
            }
            mCategoies=new ArrayList<>();
            if (mCategoriesTimer != null) {
                mCategoriesTimer.cancel();
            }
            mCategoriesTimer = new Timer();
            mCategoriesTimer.schedule(new ClassificationFragment.CategoriesTask(),time);
        }else{
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_failed_check_configuration));
            }
        }
    }

    class CategoriesTask extends TimerTask {
        @Override
        public void run() {
            mCategoriesUtils = new HttpGetUtils(getContext(),ClassificationFragment.this, Constants.HTTP_CATEGORIES_URL, mHandler);
            mCategoriesUtils.start();
        }
    }

    @Override
    public void setResponseData(String value){
        try {
            num=0;
            mCustomDialog.hideDailog();
            CategoriesItem item = (CategoriesItem) JsonUtils
                    .jsonToBean(value, CategoriesItem.class);
            if (item != null && item.isSuccess()) {
                if(item.getData()!=null){
                    mCategoies=java.util.Arrays.asList(item.getData().getCategories());
                }
            }
            // 设置适配器
            mClassificationAdapter.setList(mCategoies);
        }catch(Exception e){
            e.printStackTrace();
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_exceptions)+e.toString());
            }
        }
    }

    @Override
    public void setFailedResponse(String value){
        num=0;
        mCustomDialog.hideDailog();
        mClassificationAdapter.setList(mCategoies);
        if(isAdded()) {
            ToastUtils.showToast(getContext(), getString(R.string.network_exceptions_again)+value);
        }
    }

    @Override
    public void setTimeoutResponse(String value){
        if(num<3){
            startTimer(1000);
        }else{
            num=0;
            mCustomDialog.hideDailog();
            mClassificationAdapter.setList(mCategoies);
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_fail_again)+value);
            }
        }
    }
}
