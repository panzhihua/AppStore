package com.rongyan.appstore.utils;

import android.util.Log;
/**
 * Log 输出类
 */
public class LogUtils {

	//public final static boolean isShow = false;//上线模式

    public final static boolean isShow = true;//开发模式
  
    public static void v(String tag, String msg){  
        if(isShow == true){  
            Log.v(tag, msg);  
        }  
    }  
  
    public static void d(String tag, String msg){  
        if(isShow == true){  
            Log.d(tag, msg);  
        }  
    }  
  
    public static void i(String tag, String msg){  
        if(isShow == true){  
            Log.i(tag, msg);  
        }  
    }  
  
    public static void w(String tag, String msg){  
        if(isShow == true){  
            Log.w(tag, msg);  
        }  
    }  
      
    public static void e(String tag, String msg){  
        if(isShow == true){  
            Log.e(tag, msg);  
        }  
    }  
  
    public static void wtf(String tag, String msg){  
        if(isShow == true){  
            Log.wtf(tag, msg);  
        }  
    }  
}
