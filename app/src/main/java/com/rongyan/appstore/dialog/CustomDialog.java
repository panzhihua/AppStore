package com.rongyan.appstore.dialog;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;

import com.rongyan.appstore.R;;


/**
 * 自定义toast类型弹出框
 */

public class CustomDialog {

    private Context mContext;

    private Dialog dialog;

    private int dialogNum=0;

    private Handler handler = new Handler();

    private boolean isFirst=false;

    public CustomDialog(Context context) {
        mContext=context;
        if(!isFirst) {
            isFirst=true;
            handler.postDelayed(runnable, 5000); //每隔1s执行
        }
    }

    public void showDailog() {
        try {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (dialog != null) {
                        if (!dialog.isShowing()) {
                            dialog.show();
                            dialogNum++;
                        } else {
                            dialogNum++;
                        }
                    } else {
                        dialog = new Dialog(mContext, R.style.AlertDialog);
                        View vv = LayoutInflater.from(mContext).inflate(R.layout.dialog_normal_layout, null);

                        dialog.setCanceledOnTouchOutside(true);
                        dialog.setContentView(vv);
                        dialog.show();
                        dialogNum++;
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }


    public void hideDailog(){
        try {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    dialogNum--;
                    if (dialog != null) {
                        if (dialogNum < 1) {
                            dialogNum = 0;
                            dialog.dismiss();
                        }
                    }
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // handler自带方法实现定时器
            try {
                if(dialogNum>0){
                    hideDailog();
                }
                handler.postDelayed(this, 3000);
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    };

}
