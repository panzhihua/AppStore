package com.rongyan.appstore.utils;

import android.content.Context;
import android.os.Handler;
import android.widget.Toast;

/**
 * Toast工具类
 */

public class ToastUtils {

    private static Toast mToast;

    public static void showToast(final Context context,final String text) {
        try {
            if (mToast == null) {
                mToast = Toast.makeText(context, text, Toast.LENGTH_SHORT);
            } else {
                mToast.setText(text);
                mToast.setDuration(Toast.LENGTH_SHORT);
            }
            mToast.show();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
