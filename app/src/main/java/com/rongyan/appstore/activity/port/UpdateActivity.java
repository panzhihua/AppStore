package com.rongyan.appstore.activity.port;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

import com.rongyan.appstore.R;;

/**
 * 应用更新activity(竖屏)
 */

public class UpdateActivity extends AppCompatActivity implements HttpPostUtils.CallBack{

    private final static String TAG="UpdateActivity";

    private ImageView activity_Update_Back_Img;

    private Button activity_Update_Btn;

    private LinearLayout activity_Update_Liy;

    private DataBaseOpenHelper mDataBaseOpenHelper;

    private Timer mUpdateTimer;

    private HttpPostUtils mUpdateUtils;

    private Handler mHandler = new Handler();

    private List<AppView> mAppViewItem;

    private CustomDialog mCustomDialog;

    private int num=0;

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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);
        init();
    }

    @Override
    public void onResume(){
        ApplicationUtils.setActivity(this);
        if(!isFirst){
            judgeCheck_Update();
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
        activity_Update_Back_Img = (ImageView) findViewById(R.id.activity_update_back_img);
        activity_Update_Btn = (Button) findViewById(R.id.activity_update_btn);
        activity_Update_Liy = (LinearLayout) findViewById(R.id.activity_update_liy);
    }

    private void initEvent(){
        activity_Update_Back_Img.setOnClickListener(new View.OnClickListener() {
                @Override
            public void onClick(View v) {
                finish();
            }
        });
        activity_Update_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAppViewItem!=null&&!mAppViewItem.isEmpty()){
                    for(AppView appview:mAppViewItem){
                        if(appview.getView_App_Btn().isEnabled()){
                            appview.downAPK();
                        }
                    }
                }
            }
        });
    }

    private void initData(){
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(getApplicationContext());
        if(mCustomDialog==null) {
            mCustomDialog = new CustomDialog(UpdateActivity.this);
        }
        judgeCheck_Update();
    }

    public void startTimer(String data) {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            if(!isFinishing()) {
                mCustomDialog.showDailog();
            }
            if (mUpdateTimer != null) {
                mUpdateTimer.cancel();
            }
            mUpdateTimer = new Timer();
            mUpdateTimer.schedule(new UpdateTask(data),0);
        }else{
            ToastUtils.showToast(getApplicationContext(), getString(R.string.network_failed_check_configuration));
        }
    }

    class UpdateTask extends TimerTask {
        private String mData;

        UpdateTask(String data){
            this.mData=data;
        }
        @Override
        public void run() {
            mUpdateUtils = new HttpPostUtils(getApplicationContext(),UpdateActivity.this, Constants.HTTP_CHECK_UPDATE_URL, mHandler,mData);
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
    private void postCheck_Update() {
        try {
            if(ApplicationUtils.getAppInfos()!=null&&!ApplicationUtils.getAppInfos().isEmpty()){
                List<HashMap<String, Object>> list=new ArrayList<>();
                for(AppInfoItem mAppInfoItem:ApplicationUtils.getAppInfos()){
                    HashMap<String, Object> map = new HashMap<>();
                    map.put("package_name", mAppInfoItem.getAppPackageName());//软件包名称
                    map.put("version_code",mAppInfoItem.getVersionCode());//版本号
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
            num=0;
            mCustomDialog.hideDailog();
            LatestVersionsItem item = (LatestVersionsItem) JsonUtils
                    .jsonToBean(value, LatestVersionsItem.class);
            if (item != null && item.isSuccess()) {
                activity_Update_Liy.removeAllViews();
                mAppViewItem=new ArrayList<>();
                ApplicationUtils.setIsCheck_Update(false);
                if(item.getData()!=null&&item.getData().getApps()!=null&&item.getData().getApps().length>0){
                    if(mDataBaseOpenHelper.QueryBeingClass(DatabaseColume.UPDATEID,DatabaseColume.PAGE) ){//判断数据库中是否已存在
                        mDataBaseOpenHelper.UpdateClass(DatabaseColume.UPDATEID, value,DatabaseColume.PAGE);//更新
                    }else{
                        mDataBaseOpenHelper.AddClass(DatabaseColume.UPDATEID, value,DatabaseColume.PAGE);//添加
                    }
                    for (int i = 0; i < item.getData().getApps().length; i++) {
                        View convertView = LayoutInflater.from(UpdateActivity.this).inflate(
                                R.layout.app_item, null);
                        AppView mAppView = (AppView) convertView.findViewById(R.id.app_appview);
                        mAppView.setView(null, item.getData().getApps()[i]);
                        ApplicationUtils.setAppState(mAppView, item.getData().getApps()[i]);
                        mAppViewItem.add(mAppView);
                        activity_Update_Liy.addView(convertView);
                        if (ApplicationUtils.getAppMap().get("package:" + item.getData().getApps()[i].getPackage_name()) == null) {
                            ApplicationUtils.setAppMap("package:" + item.getData().getApps()[i].getPackage_name(), item.getData().getApps()[i]);
                        }
                    }
                }
            }
        }catch(Exception e){
            num=0;
            e.printStackTrace();
            ToastUtils.showToast(getApplicationContext(), getString(R.string.network_exceptions)+e.toString());
        }
    }

    @Override
    public void setPostFailedResponse(String value) {
        num=0;
        mCustomDialog.hideDailog();
        ToastUtils.showToast(getApplicationContext(), getString(R.string.network_exceptions_again)+value);
    }

    @Override
    public void setPostTimeoutResponse(String value) {
        if(num<3){
            postCheck_Update();
        }else{
            num=0;
            mCustomDialog.hideDailog();
            ToastUtils.showToast(getApplicationContext(), getString(R.string.network_fail_again)+value);
        }
    }

}
