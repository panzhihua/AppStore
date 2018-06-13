package com.rongyan.appstore.fragment.port;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.rongyan.appstore.activity.port.OrderActivity;
import com.rongyan.appstore.activity.port.UpdateActivity;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.database.DatabaseColume;
import com.rongyan.appstore.item.AppInfoItem;
import com.rongyan.appstore.item.LatestVersionsItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.HttpPostUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;;

/**
 * 竖屏设置Fragment
 */

public class SettingFragment extends Fragment implements HttpPostUtils.CallBack{

    private final static String TAG="SettingFragment";

    private FrameLayout fragment_Setting_Update_Fly,fragment_Setting_Order_Fly;

    private TextView fragment_Setting_Version_Txt;

    private Button fragment_Setting_Num_Btn;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private Timer mUpdateTimer;

    private HttpPostUtils mUpdateUtils;

    private Handler mHandler = new Handler();

    private int num=0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_setting, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onResume(){
        judgeCheck_Update();
        super.onResume();
    }

    public void init(){
        initView();
        initEvent();
        initData();
    }

    public void initView(){
        fragment_Setting_Update_Fly=(FrameLayout)getView().findViewById(R.id.fragment_setting_update_fly);
        fragment_Setting_Order_Fly=(FrameLayout)getView().findViewById(R.id.fragment_setting_order_fly);
        fragment_Setting_Version_Txt=(TextView)getView().findViewById(R.id.fragment_setting_version_txt);
        fragment_Setting_Num_Btn=(Button)getView().findViewById(R.id.fragment_setting_num_btn);
    }

    public void initEvent(){
        fragment_Setting_Update_Fly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), UpdateActivity.class);
                startActivity(intent);
            }
        });
        fragment_Setting_Order_Fly.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), OrderActivity.class);
                startActivity(intent);
            }
        });
    }

    public void initData(){
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getContext());
        fragment_Setting_Version_Txt.setText(getString(R.string.current_version)+ ApplicationUtils.getAppVersion(getContext()));
    }

    public void startTimer(String data) {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            if (mUpdateTimer != null) {
                mUpdateTimer.cancel();
            }
            mUpdateTimer = new Timer();
            mUpdateTimer.schedule(new UpdateTask(data),0);
        }
    }

    class UpdateTask extends TimerTask {
        private String mData;

        UpdateTask(String data){
            this.mData=data;
        }
        @Override
        public void run() {
            mUpdateUtils = new HttpPostUtils(getContext(),SettingFragment.this, Constants.HTTP_CHECK_UPDATE_URL, mHandler,mData);
            mUpdateUtils.start();
        }
    }
    /**
     * 判断是否需要重新查询
     */
    private void judgeCheck_Update() {
        if(ApplicationUtils.isIsCheck_Update()||mDataBaseOpenHelper.QueryOverdueClass(DatabaseColume.UPDATEID,DatabaseColume.PAGE)){
            mDataBaseOpenHelper.DeleteClass(DatabaseColume.UPDATEID);//删除相应类别id下所有内容
            postCheck_Update();
        }else{
            List<String> valueList=mDataBaseOpenHelper.QueryClass(DatabaseColume.UPDATEID);
            if(valueList!=null&&!valueList.isEmpty()) {
                for(String value:valueList) {
                    setPostResponseData(value);
                }
            }else{
                postCheck_Update();
            }
        }
    }
    /**
     * 应用最新版本查询
     */
    public void postCheck_Update() {
        try {
            if(ApplicationUtils.getAppInfos()!=null&&!ApplicationUtils.getAppInfos().isEmpty()){
                List<HashMap<String, Object>> list=new ArrayList<>();
                for(AppInfoItem mAppInfoItem:ApplicationUtils.getAppInfos()){
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("package_name", mAppInfoItem.getAppPackageName());//软件包名称
                    map.put("version_code", mAppInfoItem.getVersionCode());//版本号
                    map.put("version_name", mAppInfoItem.getVersionName());//版本名称
                    list.add(map);
                }
                HashMap<String,  List<HashMap<String, Object>>> appMap = new HashMap<>();
                appMap.put("packages", list);
                String mString = JsonUtils.beanToJson(appMap);
                startTimer(mString);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setPostResponseData(String value) {
        LogUtils.w(TAG,value);
        try {
            LatestVersionsItem item = (LatestVersionsItem) JsonUtils
                    .jsonToBean(value, LatestVersionsItem.class);
            if (item != null && item.isSuccess()) {
                if(item.getData()!=null){
                    if(mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.UPDATEID,DatabaseColume.PAGE) ){//判断数据库中是否已存在
                        mDataBaseOpenHelper.UpdateClass(DatabaseColume.UPDATEID, value,DatabaseColume.PAGE);//更新
                    }else{
                        mDataBaseOpenHelper.AddClass(DatabaseColume.UPDATEID, value,DatabaseColume.PAGE);//添加
                    }
                    ApplicationUtils.setIsCheck_Update(false);
                    setViewNum(item.getData().getApps().length);
                    return;
                }
            }
            setViewNum(0);
        }catch(Exception e){
            setViewNum(0);
            e.printStackTrace();
        }
    }

    @Override
    public void setPostFailedResponse(String value) {
        setViewNum(0);
    }

    @Override
    public void setPostTimeoutResponse(String value) {
        if(num<3){
            postCheck_Update();
        }else{
            setViewNum(0);
        }
    }

    private void setViewNum(int number){
        num=0;
        if(number<1){
            fragment_Setting_Num_Btn.setVisibility(View.GONE);
        }else{
            fragment_Setting_Num_Btn.setVisibility(View.VISIBLE);
            fragment_Setting_Num_Btn.setText(String.valueOf(number));
        }
    }
}
