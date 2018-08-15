package com.rongyan.appstore.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import java.net.URL;


/**
 * 应用自身下载升级类
 */

public class HttpDownLoadAPKUtils extends Thread{

    private final static String TAG="HttpDownLoadAPKUtils";
    // 下载文件 存放目的地
    private String downloadPath = Environment.getExternalStorageDirectory()
            .getPath() + "/AppStore/upgrade_apk/";

    private String address;

    private Context context;

    private long contentLength = 0;//服务器返回的数据长度

    private long readSize = 0L;//已下载的总大小

    public HttpDownLoadAPKUtils(Context context, String address) {
        this.context=context;
        this.address = address;
    }
    @Override
    public void run() {
        File tmpFile = new File(downloadPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        String[] strArray =address.split("/");
        String apkNameString=strArray[strArray.length-1];
        final File file = new File(downloadPath + apkNameString);
        try {
            if(file.exists()){//判断apk是否已经存在
                readSize=file.length();
            }
            URL url = new URL(address);
            HttpURLConnection conn=null,connTwo=null;
            InputStream is = null;
            RandomAccessFile raf=null;
            try {
                connTwo = (HttpURLConnection) url
                        .openConnection();
                connTwo.setReadTimeout(5000);
                connTwo.setConnectTimeout(5000);
                contentLength=connTwo.getContentLength();
                if(contentLength<=readSize){
                    Intent intent = new Intent("action.install.apk");
                    intent.putExtra("path", downloadPath+ apkNameString);
                    context.sendBroadcast(intent);
                }else {
                    conn = (HttpURLConnection) url
                            .openConnection();
                    conn.setReadTimeout(5000);
                    conn.setConnectTimeout(5000);
                    conn.setRequestProperty("Range", "bytes=" + readSize + "-" + (contentLength - 1));
                    is = conn.getInputStream();
                    raf = new RandomAccessFile(file, "rwd");
                    raf.seek(readSize);
                    byte[] buf = new byte[8192];
                    conn.connect();
                    if (conn.getResponseCode() == 206) {
                        while (true) {
                            if (is != null) {
                                int numRead = is.read(buf);
                                if (numRead <= 0) {
                                    break;
                                } else {
                                    raf.write(buf, 0, numRead);
                                }
                            } else {
                                break;
                            }
                        }
                        LogUtils.w(TAG, "download successful");
                        Intent intent = new Intent("action.install.apk");
                        intent.putExtra("path", downloadPath + apkNameString);
                        context.sendBroadcast(intent);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (raf != null) {
                    try {
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (conn != null) {
                    try {
                        conn.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if (connTwo != null) {
                    try {
                        connTwo.disconnect();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
