package com.rongyan.appstore.widget;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.rongyan.appstore.R;
import com.rongyan.appstore.database.DataBaseOpenHelper;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.item.AppInfoItem;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.utils.ApkUtils;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.HttpDownAPKUtils;
import com.rongyan.appstore.utils.HttpPostUtils;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.OkHttpDownAPKUtils;
import com.rongyan.appstore.utils.PermissionUtils;
import com.rongyan.appstore.utils.StringUtils;
import com.rongyan.appstore.utils.ToastUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

;

/**
 * 自定义应用类view
 */

public class AppView extends FrameLayout implements HttpPostUtils.CallBack,OkHttpDownAPKUtils.progress,HttpDownAPKUtils.progress {

    private final static String TAG="AppView";

    public final static int OPEN=0;//打开

    public final static int DOWN=1;//下载

    public final static int DOWNING=2;//下载中

    public final static int INSTALL=3;//安装

    public final static int INSTALLING=4;//安装中

    public final static int UPDATE=5;//更新

    public final static int UNINSTALL=6;//卸载

    private int type_http=1,type_okhttp=2;

    private int type_download=1;

    private boolean isDetail;//是否在竖屏详情页面

    private FrameLayout view_App_Fly;

    private LinearLayout view_App_Star_Liy;

    private Button view_App_Btn;

    private TextView view_App_Name_Txt,view_App_Time_Txt,view_App_Size_Txt;

    private ImageView view_App_Img;

    private Handler mHandler = new Handler();

    private DataBaseOpenHelper mDataBaseOpenHelper=null;

    private Timer mDownloadTimer;

    private HttpPostUtils mDownloadUtils;

    private OkHttpDownAPKUtils mOkHttpDownAPKUtils=null;

    private HttpDownAPKUtils mHttpDownAPKUtils=null;

    private Apps mApp;

    private Context mContext;

    private int num=0;//记录网络请求次数

    private app mAppNo;

    private int lastState=-1;//记录app状态

    private String appName="";//apk名字

    private int lastNum=-1;//记录app下载进度

    private boolean isBroadCast=false;//是否已注册广播

    private int install_type=1;//下载方式

    private float mPosX ,mCurPosX;

    private int down_time;//按下按钮时间

    private int install_update=DOWN;//下载还是更新

    BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, final Intent intent) {
            String action = intent.getAction();
            if (action.equals("action.update.appview")) {
                if(mApp!=null&&mApp.getNo()!=null&&intent.getExtras().getString("app_no").equals(mApp.getNo())) {
                    if((lastState!=intent.getExtras().getInt("state")||lastNum!=intent.getExtras().getInt("num"))&&intent.getExtras().getInt("state")>-1) {
                        if(lastState==OPEN&&intent.getExtras().getInt("state")==INSTALLING) {

                        }else{
                           setState(intent.getExtras().getInt("state"), intent.getExtras().getInt("num"));
                        }
                    }
                }
            }else if(action.equals("action.update.ratings")){//更新评分信息
                if(mApp!=null&&mApp.getNo()!=null&&intent.getExtras().getString("app_no").equals(mApp.getNo())) {
                    mApp.setRatings_count(intent.getExtras().getInt("ratings_count"));
                    mApp.setRatings_sum(intent.getExtras().getInt("ratings_sum"));
                    int rating;
                    if(mApp.getRatings_count()!=0) {
                        rating = mApp.getRatings_sum() / mApp.getRatings_count();
                    }else{
                        rating=0;
                    }
                    view_App_Star_Liy.removeAllViews();
                    for (int i = 0; i < rating; i++) {
                        View view = LayoutInflater.from(mContext).inflate(
                                R.layout.view_app_star, null);
                        view_App_Star_Liy.addView(view);
                    }
                    for (int i = rating; i < 5; i++) {
                        View view = LayoutInflater.from(mContext).inflate(
                                R.layout.view_app_unstar, null);
                        view_App_Star_Liy.addView(view);
                    }
                }
            }else if(action.equals("action.update.installs")){//更新下载信息
                if(mApp!=null&&mApp.getNo()!=null&&intent.getExtras().getString("app_no").equals(mApp.getNo())) {
                    mApp.setInstalled_times(intent.getExtras().getInt("installs")+"");
                    view_App_Time_Txt.setText(intent.getExtras().getInt("installs") + mContext.getString(R.string.installs));
                }
            }else if(action.equals("package.install.returncode")){//获取静默安装结果
               LogUtils.w(TAG,"returncode:"+intent.getExtras().getString("name")+"==="+mApp.getPackage_name());
                if(mApp!=null&&intent.getExtras().getString("name")!=null&&intent.getExtras().getString("name").equals(mApp.getPackage_name())) {
                    LogUtils.w(TAG,"code="+intent.getExtras().getInt("code"));
                    updateDataBase(0);
                    if(intent.getExtras().getInt("code")== 1){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装成功");
                    }else if(intent.getExtras().getInt("code")==-1){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，apk已存在");
                        deleteFile();
                        sendBroadcast(OPEN,0);
                    }else if(intent.getExtras().getInt("code")==-2){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，无效的apk");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-3){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，无效的链接");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-4){
                        ApplicationUtils.setIsInstall(false);
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，设备没有足够的存储空间来安装app");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-5){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，已经存在相同包名的apk");
                        sendBroadcast(OPEN,0);
                    }else if(intent.getExtras().getInt("code")==-6){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，请求的共享用户没有存在");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-7){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，先前安装的apk和现在更新的apk签名不一致");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-8){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，共享用户不兼容");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-9){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，共享库已丢失");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-10){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，替换时无法删除");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-11){
                        ApplicationUtils.setIsInstall(false);
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，空间不足或验证失败");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-12){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，系统版本过旧");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-13){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败， 存在同名的内容提供者");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-14){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，系统版本过新");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-15){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，调用者不被允许测试的测试程序");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-16){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，包含的本机代码不兼容CPU_ABI");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-17){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，apk使用了一个不可用的特性");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-18){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，SD卡访问失败");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-19){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，无效的安装路径");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-20){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，SD卡不可用");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-21){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，apk验证超时");
                        sendBroadcast(INSTALL,0);
                    }else if(intent.getExtras().getInt("code")==-22){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，apk验证失败");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-23){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，预期的应用被改变");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-100){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，不是APK");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-101){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，无法提取Manifest");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-102){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，无法预期的异常");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-103){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，找不到证书");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-104){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，证书不一致");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-105){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，证书编码异常");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-106){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，manifest中的包名错误或丢失");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-107){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，manifest中的共享用户错误");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-108){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，manifest中出现结构性错误");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-109){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，manifest中没有actionable tags");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }else if(intent.getExtras().getInt("code")==-110){
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，解析失败，系统问题导致安装失败");
                        sendBroadcast(INSTALL,0);
                    }else {
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }
                }else if(mApp!=null&&intent.getExtras().getString("name")==null){
                    if(lastState==INSTALLING) {
                        setBroadcast(intent.getExtras().getInt("code"));
                    }
                }
            }
        }
    };


    /**
     * 定义一个接口
     */
    public interface app{
        void getAppNo(String app_no);//返回app_no
    }

    public AppView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        this.mContext=context;
        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.AppView);
        isDetail = array.getBoolean(R.styleable.AppView_isDetail, false);
        View view;
        if(isDetail) {
            view = LayoutInflater.from(mContext).inflate(R.layout.view_appdetail_item, this);
        }else{
            view = LayoutInflater.from(mContext).inflate(R.layout.view_app_item, this);
        }
        view_App_Fly = (FrameLayout)view.findViewById(R.id.view_app_fly);
        view_App_Img = (ImageView)view.findViewById(R.id.view_app_img);
        view_App_Btn = (Button)view.findViewById(R.id.view_app_btn);
        view_App_Star_Liy = (LinearLayout)view.findViewById(R.id.view_app_star_liy);
        view_App_Name_Txt = (TextView)view.findViewById(R.id.view_app_name_txt);
        view_App_Time_Txt = (TextView)view.findViewById(R.id.view_app_time_txt);
        view_App_Size_Txt = (TextView)view.findViewById(R.id.view_app_size_txt);
        init();
        initEvent();
    }

    private void init(){
        mDataBaseOpenHelper = DataBaseOpenHelper.getInstance(mContext);
    }

    private void initEvent() {
//        view_App_Btn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mApp!=null) {
//                    if(lastState==OPEN){//打开
//                        Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mApp.getPackage_name());
//                        mContext.startActivity(intent);
//                    }else if(lastState==DOWN){//下载
//                        downAPK();
//                    }else if(lastState==INSTALL){//安装
//                        if(ApplicationUtils.isIsInstall()&&ApplicationUtils.isDownLoad(mApp)) {
//                            if (mContext.getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {//竖版
//                                Intent intent = new Intent(Intent.ACTION_VIEW);
//                                intent.setDataAndType(Uri.fromFile(new File(HttpDownAPKUtils.downloadPath + appName)),
//                                        "application/vnd.android.package-archive");
//                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                mContext.startActivity(intent);
//                            } else { //横版
//                                sendBroadcastInstall(INSTALLING, 0, mApp.getNo());
//                            }
//                        }else{
//                            ToastUtils.showToast(getContext(), mContext.getString(R.string.remaining_storage_space_insufficient_downloaded));
//                        }
//                    }else if(lastState==UPDATE){//更新
//                        downAPK();
//                    }else if(lastState==UNINSTALL){//卸载
//                        uninstallAPK();
//                    }
//                }
//            }
//        });
//        view_App_Btn.setOnLongClickListener(new OnLongClickListener() {
//            @Override
//            public boolean onLongClick(View v) {
//                if(mApp!=null) {
//                    if(lastState==DOWN){//下载
//                        System.out.println("====下载");
//                        downAPKOk();
//                    }else if(lastState==UPDATE){//更新
//                        System.out.println("====更新");
//                        downAPKOk();
//                    }
//                }
//                return true;
//            }
//        });
        view_App_Fly.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mAppNo!=null) {
                    mAppNo.getAppNo(mApp.getNo());
                }
            }
        });
        view_App_Btn.setOnTouchListener(new OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        down_time=StringUtils.getSystemTime();
                        mPosX = event.getX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        break;
                    case MotionEvent.ACTION_UP:
                        mCurPosX = event.getX();
                        LogUtils.w(TAG,"mCurPosX - mPosX="+(mCurPosX - mPosX));
                        if (mCurPosX - mPosX > 20) {
//                            if(lastState==UNINSTALL) {
//                                setState(OPEN, 0);
//                            }
                        } else if (mCurPosX - mPosX < -20) {
//                            if(lastState==OPEN) {
//                                setState(UNINSTALL, 0);
//                            }
                        }else{
                            if(lastState==OPEN){//打开
                                LogUtils.w(TAG,"StringUtils.getSystemTime()-down_time="+(StringUtils.getSystemTime()-down_time));
                                if(StringUtils.getSystemTime()-down_time>0) {
                                    setState(UNINSTALL, 0);
                                }else{
                                    Intent intent = mContext.getPackageManager().getLaunchIntentForPackage(mApp.getPackage_name());
                                    if(intent==null){
                                        ToastUtils.showToast(getContext(), mContext.getString(R.string.application_cannot_opened));
                                    }else {
                                        mContext.startActivity(intent);
                                    }
                                }
                            }else if(lastState==DOWN){//下载
                                if(StringUtils.getSystemTime()-down_time>1) {
                                    downAPKOk();
                                }else{
                                    downAPK();
                                }
                            }else if(lastState==INSTALL){//安装
                                if(ApplicationUtils.isIsInstall()&&ApplicationUtils.isDownLoad(mApp)) {
                                    sendBroadcastInstall(INSTALLING, 0, mApp.getNo());
                                }else{
                                    ToastUtils.showToast(getContext(), mContext.getString(R.string.remaining_storage_space_insufficient_downloaded));
                                }
                            }else if(lastState==UPDATE){//更新
                                if(StringUtils.getSystemTime()-down_time>1) {
                                    downAPKOk();
                                }else{
                                    downAPK();
                                }
                            }else if(lastState==UNINSTALL){//卸载
                                LogUtils.w("panzhihua",StringUtils.getSystemTime()+"=="+down_time);
                                if(StringUtils.getSystemTime()-down_time>1) {
                                    setState(OPEN, 0);
                                }else {
                                    uninstallAPK();
                                }
                            }
                        }
                        break;
                    default:
                        break;
                }
                return true;
            }

        });
    }

    /**
     * 注册广播
     */
    private void initBroadCast(){
        IntentFilter filter = new IntentFilter();
        filter.addAction("action.update.appview");
        filter.addAction("action.update.ratings");
        filter.addAction("action.update.installs");
        filter.addAction("package.install.returncode");
        mContext.registerReceiver(mBroadcastReceiver, filter);
        isBroadCast=true;
    }

    public void setView(Object object,Apps apps){
        if(isBroadCast) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            isBroadCast = false;
        }
        mAppNo=(app)object;
        if(apps!=null){
            mApp=apps;
            Glide.with(mContext)
                    .load(mApp.getIcon_url())
                    .placeholder(R.drawable.appicon_default)
                    .error(R.drawable.appicon_default)
                    .listener(new RequestListener<String, GlideDrawable>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {

                            return false;
                        }
                        //这个用于监听图片是否加载完成
                        @Override
                        public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

                            return false;
                        }
                    }).into(view_App_Img);
            view_App_Name_Txt.setText(mApp.getName());
            view_App_Size_Txt.setText(StringUtils.formetFileSize(mApp.getPackage_size()));
            String[] sourceStrArray = mApp.getPackage_url().split("/");
            appName=sourceStrArray[sourceStrArray.length-1];
            setAppInfo();
            if(!isBroadCast) {
                initBroadCast();
            }
        }
    }

    public void setAppInfo(){
        AppInfo appInfo=mDataBaseOpenHelper.QueryApp(mApp.getNo());
        if(appInfo==null) {
            appInfo=new AppInfo();
            appInfo.setInstalled_times(mApp.getInstalled_times());
            if(mApp.getRatings_count()>0) {
                appInfo.setRatings(mApp.getRatings_sum() / mApp.getRatings_count());
            }else{
                appInfo.setRatings(0);
            }
        }
        view_App_Time_Txt.setText(appInfo.getInstalled_times() + mContext.getString(R.string.installs));
        view_App_Star_Liy.removeAllViews();
        for (int i = 0; i < appInfo.getRatings(); i++) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.view_app_star, null);
            view_App_Star_Liy.addView(view);
        }
        for (int i = appInfo.getRatings(); i < 5; i++) {
            View view = LayoutInflater.from(mContext).inflate(
                    R.layout.view_app_unstar, null);
            view_App_Star_Liy.addView(view);
        }

    }

    public String getAppName() {
        return appName;
    }

    public Apps getmApp() {
        return mApp;
    }

    public Button getView_App_Btn() {
        return view_App_Btn;
    }

    public void downAPK(){
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (ApplicationUtils.isDownLoad(mApp) && ApplicationUtils.isIsInstall()) {
                if (ApplicationUtils.ismNetWorkEnable() && view_App_Btn.isEnabled()) {
                    install_type = type_http;
                    putProgress(0, DOWNING, mApp.getNo());
                    view_App_Btn.setEnabled(false);
                    updateDataBase(2);
                    mHttpDownAPKUtils = new HttpDownAPKUtils(mContext, AppView.this, mApp, appName, DOWNING);
                    mHttpDownAPKUtils.start();
                }
            } else {
                ToastUtils.showToast(getContext(), mContext.getString(R.string.remaining_storage_space_insufficient_downloaded));
            }
        }else{
            downAPKPermission();
        }
    }

    public void uninstallAPK(){
        Intent intent = new Intent();
        intent.setAction("action.delete.apk");
        intent.putExtra("package_name", mApp.getPackage_name());
        mContext.sendBroadcast(intent);
    }

    public void downAPKPermission(){
        Intent intent = new Intent();
        intent.setAction("action.app.permission");
        intent.putExtra("type", PermissionUtils.CODE_WRITE_EXTERNAL_STORAGE);
        mContext.sendBroadcast(intent);
    }

    public void downAPKOk(){
        if (ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            if (ApplicationUtils.isDownLoad(mApp) && ApplicationUtils.isIsInstall()) {
                if (ApplicationUtils.ismNetWorkEnable() && view_App_Btn.isEnabled()) {
                    install_type = type_okhttp;
                    putProgress(0, DOWNING, mApp.getNo());
                    view_App_Btn.setEnabled(false);
                    updateDataBase(2);
                    mOkHttpDownAPKUtils = new OkHttpDownAPKUtils(mContext, AppView.this, mApp, appName, DOWNING);
                    mOkHttpDownAPKUtils.download();
                }
            } else {
                ToastUtils.showToast(getContext(), mContext.getString(R.string.remaining_storage_space_insufficient_downloaded));
            }
        }else{
            downAPKPermission();
        }
    }

    public void setState(int state,int num) {
        LogUtils.w(TAG,"appname=" + mApp.getName() + "==state=" + state+"==num="+num);
        try {
            lastState = state;
            lastNum=num;
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    setButton(lastState, lastNum);
                }
            });
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    /**
     * 发送广播
     */
    public void sendBroadcast(int state,int num,String app_no){
        if(mApp!=null) {
            if(state==INSTALL){
                AppInfo appInfo = mDataBaseOpenHelper.QueryApp(app_no);
                if (appInfo != null) {
                    if (appInfo.getInstalling() == 1) {//安装中
                        state = INSTALLING;
                    }
                }
            }
            if(num!=0&&(state==DOWN||state==UPDATE)){
                AppInfo appInfo=mDataBaseOpenHelper.QueryApp(app_no);
                if(appInfo!=null){
                    if(appInfo.getInstalling()==2){//下载中
                        state=DOWNING;
                    }else{
                        num=0;
                    }
                }else{
                    num=0;
                }
            }
            if(state==DOWN&&getInstall_update()==UPDATE) {
                state = UPDATE;
            }
            Intent intent = new Intent();
            intent.setAction("action.update.appview");
            intent.putExtra("state", state);
            intent.putExtra("num", num);
            intent.putExtra("app_no", app_no);
            mContext.sendBroadcast(intent);
        }
    }

    public void sendBroadcast(int state,int num){
        sendBroadcast(state,num,mApp.getNo());
    }

    public void setBroadcast(int code) {
        if (mApp != null) {
            AppInfo appInfo = mDataBaseOpenHelper.QueryApp(mApp.getNo());
            if (appInfo != null) {
                if (appInfo.getInstalling() == 1) {
                    updateDataBase(0);
                    ApplicationUtils.setAppState(this,mApp);
                    if(code==-4||code==-11){
                        ApplicationUtils.setIsInstall(false);
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败，设备没有足够的存储空间来安装app");
                    }else{
                        ToastUtils.showToast(mContext,mApp.getName()+"安装失败");
                        deleteFile();
                        sendBroadcast(DOWN,0);
                    }
                }
            }
        }
    }

    /**
     * 发送广播(安装中)
     */
    public void sendBroadcastInstall(int state,int num,String app_no){
        boolean isIntegrity=true;
//        Map<String,Object> mapApk = ApkUtils.readAPK(OkHttpDownAPKUtils.downloadPath+appName);
//        if(!mapApk.isEmpty()) {
//            for (String key : mapApk.keySet()) {
//                if (mapApk.get(key) == null || mapApk.get(key).equals("")) {
//                    isIntegrity = false;
//                    break;
//                }
//            }
//        }else{
//            isIntegrity = false;
//        }
        File file = new File(HttpDownAPKUtils.downloadPath + appName);
        if(mApp.getPackage_md5()==null||(mApp.getPackage_md5()!=null&&!mApp.getPackage_md5().contains(ApkUtils.getFileMD5(file)))){
            isIntegrity = false;
        }
        LogUtils.w(TAG,ApkUtils.getFileMD5(file)+"=="+mApp.getPackage_md5());
        if(isIntegrity) {
            //先修改显示文字
            sendBroadcast(state, num, app_no);
            //再取消安装限制
            Intent install_intent = new Intent();
            install_intent.setAction("android.apps.write.list");
            install_intent.putExtra("keys", mApp.getPackage_name());
            mContext.sendBroadcast(install_intent);
            //然后静默安装
            Intent intent = new Intent();
            intent.setAction("action.install.apk");
            intent.putExtra("path", HttpDownAPKUtils.downloadPath + appName);
            intent.putExtra("package_name", mApp.getPackage_name());
            mContext.sendBroadcast(intent);
            //最后修改数据库状态
            updateDataBase(1);
        }else{
            sendBroadcast(DOWN, 0,mApp.getNo());//下载
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    ToastUtils.showToast(mContext,mApp.getName()+"apk不合法，请重新下载");
                }
            });
            deleteFile();
        }
    }

    /**
     *  修改数据库状态
     */
    public void updateDataBase(int installing){
        AppInfo appInfo=mDataBaseOpenHelper.QueryApp(mApp.getNo());
        if(appInfo==null){
            appInfo=new AppInfo();
            appInfo.setInstalled_times(mApp.getInstalled_times());
            if(mApp.getRatings_count()>0) {
                appInfo.setRatings(mApp.getRatings_sum() / mApp.getRatings_count());
            }else{
                appInfo.setRatings(0);
            }
            appInfo.setInstalling(installing);
            mDataBaseOpenHelper.AddApp(appInfo);
        }else{
            appInfo.setInstalling(installing);
            mDataBaseOpenHelper.UpdateAppInstalling(appInfo);
        }
    }

    /**
     * 安装完毕删除下载包
     */
    private void deleteFile(){
        try {
            File file = new File(HttpDownAPKUtils.downloadPath + appName);
            if (file.exists()) {
                if(!file.delete()){
                    file.delete();
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void setButton(int state, int num){
        if(state==OPEN){//打开
            view_App_Btn.setTextColor(mContext.getResources().getColorStateList(R.color.orange_10));
            view_App_Btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.button_white_style));
            view_App_Btn.setText(mContext.getText(R.string.open));
            view_App_Btn.setEnabled(true);
        }else if(state==DOWN){//下载
            view_App_Btn.setTextColor(mContext.getResources().getColorStateList(R.color.white_10));
            view_App_Btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.button_red_style));
            view_App_Btn.setText(mContext.getText(R.string.down));
            view_App_Btn.setEnabled(true);
        }else if(state==DOWNING){//下载中
            view_App_Btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.clip_left));
            view_App_Btn.getBackground().setLevel(num);
            view_App_Btn.setTextColor(mContext.getResources().getColorStateList(R.color.gray16_8));
            view_App_Btn.setText(num/100+"%");
            view_App_Btn.setEnabled(false);
        }else if(state==INSTALL){//安装
            view_App_Btn.setTextColor(mContext.getResources().getColorStateList(R.color.white_10));
            view_App_Btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.button_red_style));
            view_App_Btn.setText(mContext.getText(R.string.install));
            view_App_Btn.setEnabled(true);
        }else if(state==INSTALLING){//安装中
            view_App_Btn.setTextColor(mContext.getResources().getColorStateList(R.color.white_10));
            view_App_Btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.button_red_style));
            view_App_Btn.setText(mContext.getText(R.string.installing));
            view_App_Btn.setEnabled(false);
        }else if(state==UPDATE){//更新
            view_App_Btn.setTextColor(mContext.getResources().getColorStateList(R.color.white_10));
            view_App_Btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.button_red_style));
            view_App_Btn.setText(mContext.getText(R.string.update));
            view_App_Btn.setEnabled(true);
        }else if(state==UNINSTALL){//卸载
            view_App_Btn.setTextColor(mContext.getResources().getColorStateList(R.color.orange_10));
            view_App_Btn.setBackground(ContextCompat.getDrawable(mContext,R.drawable.button_white_style));
            view_App_Btn.setText(mContext.getText(R.string.uninstall));
            view_App_Btn.setEnabled(true);
        }
    }

    @Override
    public void putProgress(int progress,int state,String appNo) {
        try {
            if(appNo.equals(mApp.getNo())) {
                LogUtils.w(TAG, progress + "");
                if (progress ==-2) {//说明apk已经存在
                    if (state == DOWNING) {
                        sendBroadcastInstall(INSTALLING, 0,appNo);//安装中
                    }
                }else if(progress ==-1){//说明apk下载失败
                    updateDataBase(0);
                    if(lastState==UPDATE){
                        sendBroadcast(UPDATE, 0,appNo);
                    }else {
                        sendBroadcast(DOWN, 0,appNo);
                    }
                }else {
                    sendBroadcast(state, progress*100,appNo);
                    if (progress >=100) {
                        if(install_type==type_http) {
                            mHttpDownAPKUtils.setEndTime(StringUtils.getSystemDate());
                        }else{
                            mOkHttpDownAPKUtils.setEndTime(StringUtils.getSystemDate());
                        }
                        Thread.sleep(1000);
                        putDownload();
                        if (state == DOWNING) {
                            File file = new File(HttpDownAPKUtils.downloadPath + appName);
                            if(file.exists()) {//判断apk是否已经存在,可能在下载过程中把apk删除
                                sendBroadcastInstall(INSTALLING, 0,appNo);//安装中
                            }else{
                                updateDataBase(0);
                                sendBroadcast(DOWN, 0,appNo);//下载
                            }
                        }
                    }
                }
            }
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void startTimer(int type,String data) {
        if (ApplicationUtils.ismNetWorkEnable()){
            num++;
            if (mDownloadTimer != null) {
                mDownloadTimer.cancel();
            }
            mDownloadTimer = new Timer();
            mDownloadTimer.schedule(new DownloadTask(data), 0);
        }else{
            //下载安装回调不提示客户
        }
    }

    class DownloadTask extends TimerTask {

        private String mData;

        DownloadTask(String data){
            this.mData=data;
        }
        @Override
        public void run() {
            mDownloadUtils = new HttpPostUtils(mContext,AppView.this, String.format(Constants.HTTP_DOWNLOAD_URL, mApp.getNo()), mHandler,mData);
            mDownloadUtils.start();
        }
    }

    @Override
    public void setPostResponseData(String value) {
        LogUtils.w(TAG,value);
        num=0;
    }

    @Override
    public void setPostFailedResponse(String value) {
        num=0;
    }

    @Override
    public void setPostTimeoutResponse(String value) {
        if(num<3){
            putDownload();
        }else{
            num=0;
        }
    }

    /**
     * 下载回调
     */
    private void putDownload(){
        try {
            HashMap<String, String> map = new HashMap<>();
            map.put("apk_no", mApp.getApk_no());//apk序列号
            map.put("package_name", mApp.getPackage_name());//软件包名称
            map.put("package_url", mApp.getPackage_url());//包的url
            map.put("package_size", String.valueOf(mApp.getPackage_size()));//包的大小
            map.put("version_code", String.valueOf(mApp.getVersion_code()));//版本号
            map.put("version_name", mApp.getVersion_name());//版本名称
            if(ApplicationUtils.getAppInfos()!=null&&!ApplicationUtils.getAppInfos().isEmpty()){
                for(AppInfoItem mAppInfoItem:ApplicationUtils.getAppInfos()) {
                    if(mAppInfoItem.getAppPackageName().equals(mApp.getPackage_name())) {
                        map.put("old_version_code", String.valueOf(mAppInfoItem.getVersionCode()));//机器上已经安装的版本号
                        map.put("old_version_name", mAppInfoItem.getVersionName());//机器上已经安装的版本名称
                    }
                }
            }
            if(install_type==type_http) {
                map.put("started_at", mHttpDownAPKUtils.getStartTime());//开始下载时间
                map.put("ended_at", mHttpDownAPKUtils.getEndTime());//结束下载时间
                map.put("downloaded_size", String.valueOf(mHttpDownAPKUtils.getReadSize()));//下载使用流量
            }else{
                map.put("started_at", mOkHttpDownAPKUtils.getStartTime());//开始下载时间
                map.put("ended_at", mOkHttpDownAPKUtils.getEndTime());//结束下载时间
                map.put("downloaded_size", String.valueOf(mOkHttpDownAPKUtils.getReadSize()));//下载使用流量
            }

            HashMap<String, HashMap<String, String>> appMap = new HashMap<>();
            appMap.put("download_log", map);
            String mString = JsonUtils.beanToJson(appMap);
            startTimer(type_download,mString);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onDetachedFromWindow(){
        if(isBroadCast) {
            mContext.unregisterReceiver(mBroadcastReceiver);
            isBroadCast = false;
        }
        super.onDetachedFromWindow();
    }

    public int getInstall_update() {
        return install_update;
    }

    public void setInstall_update(int install_update) {
        this.install_update = install_update;
    }
}
