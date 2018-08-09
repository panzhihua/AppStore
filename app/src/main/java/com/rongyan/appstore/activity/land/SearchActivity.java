package com.rongyan.appstore.activity.land;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.rongyan.appstore.adapter.SearchAdapter;
import com.rongyan.appstore.dialog.CustomDialog;
import com.rongyan.appstore.item.Apps;
import com.rongyan.appstore.item.SearchItem;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.HttpGetUtils;
import com.rongyan.appstore.utils.Constants;
import com.rongyan.appstore.utils.JsonUtils;
import com.rongyan.appstore.utils.LogUtils;
import com.rongyan.appstore.utils.ToastUtils;

import java.util.Timer;
import java.util.TimerTask;

import com.rongyan.appstore.R;

import static com.rongyan.appstore.utils.ApplicationUtils.getActivity;

/**
 * 横版搜索activity
 */

public class SearchActivity extends AppCompatActivity implements HttpGetUtils.CallBack{

    private final static String TAG="SearchActivity";

    private ImageView activity_Search_Back_Img;

    private EditText activity_Search_Edit;

    private LinearLayout activity_Search_Liy;

    private ListView activity_Search_Listview;

    private TextView activity_Search_Txt,activity_Search_Back_Txt;

    private SearchAdapter mSearchAdapter;

    private Handler mHandler = new Handler();

    private Timer mSearchTimer;

    private HttpGetUtils mSearchUtils;

    private CustomDialog mCustomDialog=null;

    private int num=0;

    private String mSearch;

    private boolean isFirst=true;//是否首次进入该页面

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        init();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume(){
        if(!isFirst){
            mSearchAdapter.mCount=0;
            mSearchAdapter.notifyDataSetChanged();
        }
        isFirst=false;
        super.onResume();
    }

    public void init(){
        initView();//实例化界面控件
        initEvent();
        initData();
    }

    public void initView() {
        activity_Search_Back_Img = (ImageView) findViewById(R.id.activity_search_back_img);//返回按钮
        activity_Search_Back_Txt = (TextView) findViewById(R.id.activity_search_back_txt);//返回文字
        activity_Search_Liy = (LinearLayout) findViewById(R.id.activity_search_liy);
        activity_Search_Edit = (EditText) findViewById(R.id.activity_search_edit);
        activity_Search_Listview = (ListView) findViewById(R.id.activity_search_listview);
        activity_Search_Txt = (TextView) findViewById(R.id.activity_search_txt);
    }

    public void initEvent() {
        activity_Search_Back_Img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*隐藏软键盘*/
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if (inputMethodManager.isActive()&&isFinishing()) {
                        inputMethodManager.hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                finish();
            }
        });
        activity_Search_Back_Txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*隐藏软键盘*/
                try {
                    InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    if(inputMethodManager.isActive()&&isFinishing()){
                        inputMethodManager.hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus().getWindowToken(), 0);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
                finish();
            }
        });
        activity_Search_Edit.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // 输入的内容变化的监听
                if(s.toString().equals("")){
                    activity_Search_Liy.setVisibility(View.VISIBLE);
                }else{
                    activity_Search_Liy.setVisibility(View.GONE);
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
        activity_Search_Edit.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                activity_Search_Edit.setFocusable(true);
                activity_Search_Edit.setFocusableInTouchMode(true);
                return false;
            }
        });
    }

    public void initData() {
        Intent getIntent = getIntent();
        mSearch = getIntent.getStringExtra("search_text");
        activity_Search_Edit.setText(mSearch.toCharArray(), 0, mSearch.length());
        activity_Search_Edit.setSelection(mSearch.length());//将光标移至文字末尾
        mSearchAdapter = new SearchAdapter(this,null);
        activity_Search_Listview.setAdapter(mSearchAdapter);
        mCustomDialog = new CustomDialog(getActivity());
        startTimer();
    }

    public void startTimer() {
        if (ApplicationUtils.ismNetWorkEnable()) {
            num++;
            if(!isFinishing()) {
               mCustomDialog.showDailog();
            }
            if (mSearchTimer != null) {
                mSearchTimer.cancel();
            }
            mSearchTimer = new Timer();
            mSearchTimer.schedule(new SearchTask(), 0);
        }
    }

    class SearchTask extends TimerTask {

        @Override
        public void run() {
            mSearchUtils = new HttpGetUtils(SearchActivity.this,SearchActivity.this, String.format(Constants.HTTP_SEARCH_URL,mSearch), mHandler);
            mSearchUtils.start();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        if(event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
            /*隐藏软键盘*/
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if(inputMethodManager.isActive()&&isFinishing()){
                inputMethodManager.hideSoftInputFromWindow(SearchActivity.this.getCurrentFocus().getWindowToken(), 0);
            }
            mSearch=activity_Search_Edit.getText().toString();
            if(!mSearch.equals("")&&mSearch!=null) {
                startTimer();
            }
            return true;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void setResponseData(String value) {
        LogUtils.w(TAG, value);
        try {
            finishEnd();
            SearchItem item = (SearchItem) JsonUtils
                    .jsonToBean(value, SearchItem.class);
            if (item != null && item.isSuccess()) {
                if (item.getData() != null) {
                    if (item.getData().getApps() != null && item.getData().getApps().size()>0) {
                        setView(true);
                        mSearchAdapter.setList(item.getData().getApps());
                        for (Apps app : item.getData().getApps()) {
                            if (ApplicationUtils.getAppMap().get("package:" + app.getPackage_name()) == null) {
                                ApplicationUtils.setAppMap("package:" + app.getPackage_name(), app);
                            }
                        }
                    }else{
                        setView(false);
                    }
                }else{
                    setView(false);
                }
            }else{
                setView(false);
            }
        } catch (Exception e) {
            setView(false);
            ToastUtils.showToast(SearchActivity.this, getString(R.string.network_exceptions)+e.toString());
            e.printStackTrace();
        }
    }

    @Override
    public void setFailedResponse(String value) {
        finishEnd();
        setView(false);
    }

    @Override
    public void setTimeoutResponse(String value) {
        if(num<3){
            startTimer();
        }else{
            finishEnd();
            setView(false);
            ToastUtils.showToast(SearchActivity.this, getString(R.string.network_fail_again)+value);
        }
    }

    private void finishEnd(){
        num=0;
        mCustomDialog.hideDailog();
    }

    /**
     * 控制界面显示结果
     */
    private void setView(boolean isSucceed){
        if(isSucceed){
            activity_Search_Txt.setVisibility(View.GONE);
            activity_Search_Listview.setVisibility(View.VISIBLE);
        }else{
            activity_Search_Txt.setVisibility(View.VISIBLE);
            activity_Search_Listview.setVisibility(View.GONE);
        }
    }

}
