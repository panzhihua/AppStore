package com.rongyan.appstore.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.os.StatFs;
import android.util.Log;

import static android.content.Context.MODE_PRIVATE;

public class FileUtils {

	//得到外部储存sdcard的状态
	private static String sdcard=Environment.getExternalStorageState();
	//外部储存sdcard存在的情况
	private static String state=Environment.MEDIA_MOUNTED;
	    
	private  static File file=Environment.getExternalStorageDirectory();
	    
	private  static StatFs statFs=new StatFs(file.getPath());
	/**
     * 计算Sdcard的剩余大小
     * @return MB
     */
    public static long getAvailableSize(){
        if(sdcard.equals(state)){
            //获得Sdcard上每个block的size
            long blockSize=statFs.getBlockSize();
            //获取可供程序使用的Block数量
            long blockavailable=statFs.getAvailableBlocks();
            //计算标准大小使用：1024，当然使用1000也可以
            long blockavailableTotal=blockSize*blockavailable;
            return blockavailableTotal;
        }else{
            return -1;
        }
    }

    public static String read(){
        try {
            File file = new File(Environment.getExternalStorageDirectory()
                    .getPath() + "/AppStore/boot.txt");
            if (!file.exists()) {
                return null;
            }
            FileInputStream fis=new FileInputStream(file);
            byte[] buff=new byte[1024];
            int hasRead=0;
            StringBuffer sb=new StringBuffer();
            while ((hasRead=fis.read(buff))>0){
                sb.append(new String(buff,0,hasRead,"utf-8"));
            }
            fis.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void delete(){
        try {
            File file=new File(Environment.getExternalStorageDirectory().getPath() + "/AppStore/boot.txt");
            file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
