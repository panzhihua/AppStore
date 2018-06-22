package com.rongyan.appstore.fragment.land;

import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.database.DatabaseColume;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.AppInfoItem;
import com.rongyan.appstore.item.LatestVersionsItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.HttpPostUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.ToastUtils;
import com.rongyan.appstore.widget.AppView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;

/**
 * 横屏应用更新Fragment
 */

public class UpdateFragment extends Fragment implements HttpPostUtils.CallBack{

    private final static String TAG="UpdateFragment";

    private int state_null=0,state_init=1;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private LinearLayout fragment_Update_Liy;

    private CustomDialog mCustomDialog;

    private Timer mUpdateTimer;

    private HttpPostUtils mUpdateUtils;

    private Handler mHandler = new Handler();

    private List<AppView> mAppViewItem;

    private int state=state_init;

    private int num=0;

    private boolean isFirst=true;//是否首次进入该页面
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_update, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        init();
    }

    @Override
    public void onResume(){
        if(!isFirst){
            judgeCheck_Update();
        }
        isFirst=false;
        super.onResume();
    }

    public void init(){
        initView();
        initEvent();
        initData();
    }

    public void initView(){
        fragment_Update_Liy = (LinearLayout) getView().findViewById(R.id.fragment_update_liy);
    }

    public void initEvent(){
        if(mAppViewItem!=null&&!mAppViewItem.isEmpty()){
            for(AppView appview:mAppViewItem){
                if(appview.getView_App_Btn().isEnabled()){
                    if(appview.getView_App_Btn().getText().toString().equals(getString(R.string.update))) {
                        appview.downAPK();//下载中
                    }else if(appview.getView_App_Btn().getText().toString().equals(getString(R.string.install))){
                        appview.sendBroadcastInstall(AppView.INSTALLING, 0,appview.getmApp().getNo());//安装中
                    }
                }
            }
        }
    }


    public void initData(){
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getContext());
        if(mCustomDialog==null) {
            mCustomDialog = new CustomDialog(getActivity());
        }
        judgeCheck_Update();
    }

    public void startTimer(String data) {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            state=state_init;
            if(isAdded()) {
                mCustomDialog.showDailog();
            }
            if (mUpdateTimer != null) {
                mUpdateTimer.cancel();
            }
            mUpdateTimer = new Timer();
            mUpdateTimer.schedule(new UpdateTask(data),0);
        }else{
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_failed_check_configuration));
            }
        }
    }

    class UpdateTask extends TimerTask {
        private String mData;

        UpdateTask(String data){
            this.mData=data;
        }
        @Override
        public void run() {
            mUpdateUtils = new HttpPostUtils(getContext(),UpdateFragment.this, Constants.HTTP_CHECK_UPDATE_URL, mHandler,mData);
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
                state=state_null;
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
            finish(state);
            LatestVersionsItem item = (LatestVersionsItem) JsonUtils
                    .jsonToBean(value, LatestVersionsItem.class);
            if (item != null && item.isSuccess()) {
                fragment_Update_Liy.removeAllViews();
                mAppViewItem=new ArrayList<>();
                ApplicationUtils.setIsCheck_Update(false);
                if(item.getData()!=null){
                    if(mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.UPDATEID,DatabaseColume.PAGE) ){//判断数据库中是否已存在
                        mDataBaseOpenHelper.UpdateClass(DatabaseColume.UPDATEID, value,DatabaseColume.PAGE);//更新
                    }else{
                        mDataBaseOpenHelper.AddClass(DatabaseColume.UPDATEID, value,DatabaseColume.PAGE);//添加
                    }
                    for (int i = 0; i < item.getData().getApps().length; i++) {
                        View convertView = LayoutInflater.from(getContext()).inflate(
                                R.layout.update_item, null);
                        AppView mAppView = (AppView) convertView.findViewById(R.id.update_appview);
                        mAppView.setView(null, item.getData().getApps()[i]);
                        ApplicationUtils.setAppState(mAppView, item.getData().getApps()[i]);
                        mAppViewItem.add(mAppView);
                        fragment_Update_Liy.addView(convertView);
                        if (ApplicationUtils.getAppMap().get("package:" + item.getData().getApps()[i].getPackage_name()) == null) {
                            ApplicationUtils.setAppMap("package:" + item.getData().getApps()[i].getPackage_name(), item.getData().getApps()[i]);
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
    public void setPostFailedResponse(String value) {
        finish(state);
        if(isAdded()) {
            ToastUtils.showToast(getContext(), getString(R.string.network_exceptions_again)+value);
        }
    }

    @Override
    public void setPostTimeoutResponse(String value) {
        if(num<3){
            postCheck_Update();
        }else{
            finish(state);
            if(isAdded()) {
                ToastUtils.showToast(getContext(), getString(R.string.network_fail_again)+value);
            }
        }
    }

    private void finish(int state){
        num=0;
        if(state==state_init){
            mCustomDialog.hideDailog();
        }
    }
}
