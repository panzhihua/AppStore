package com.rongyan.appstore.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;

import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * 应用自身下载升级类
 */

public class OkHttpDownLoadAPKUtils {
    // 下载文件 存放目的地
    public final static String downloadPath = Environment.getExternalStorageDirectory()
            .getPath() + "/AppStore/upgrade_apk/";

    private OkHttpClient okHttpClient = new OkHttpClient();

    private final static String TAG="OkHttpDownLoadAPKUtils";

    private String address;

    private Context context;

    public OkHttpDownLoadAPKUtils(Context context, String address) {
        this.context=context;
        this.address = address;
    }

    public void download() {
        File tmpFile = new File(downloadPath);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        final File file = new File(downloadPath + "appstore.apk");
        if(file.exists()){//判断apk是否已经存在
            file.delete();
        }
        Request request = new Request.Builder().url(address).build();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Request request, IOException e) {
                file.delete();//下载失败，删除已下载部分
            }

            @Override
            public void onResponse(Response response) throws IOException {
                InputStream is = null;
                FileOutputStream fos = null;
                try {
                    byte[] buf = new byte[2048];
                    int len = 0;
                    is = response.body().byteStream();
                    fos = new FileOutputStream(file);
                    while ((len = is.read(buf)) != -1) {
                        fos.write(buf, 0, len);
                    }
                    fos.flush();
                    // 下载完成
                    LogUtils.w(TAG, "download apk successful");
                    Intent intent = new Intent("action.install.apk");
                    intent.putExtra("path", downloadPath+ "appstore.apk");
                    context.sendBroadcast(intent);
                } catch (Exception e) {
                    file.delete();//下载失败，删除已下载部分
                } finally {
                    try {
                        if (is != null)
                            is.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        if (fos != null)
                            fos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

}
