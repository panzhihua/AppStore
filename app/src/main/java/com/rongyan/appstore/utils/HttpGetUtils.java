package com.rongyan.appstore.utils;

import java.io.BufferedReader;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.BindException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketException;
import java.net.URL;

import android.content.Context;
import android.os.Build;
import android.os.Handler;

import com.rongyan.appstore.R;


/**
 * http 通过get方式获取json数据
 */
public class HttpGetUtils extends Thread {

	private final static String TAG="HttpGetUtils";
	
	private String mURL;

	private Handler mHandler;

	private CallBack mCallBack;

	private Context mContext;

	public interface CallBack {

		void setResponseData(String value);

		void setFailedResponse(String value);
		
		void setTimeoutResponse(String value);
	}

	public HttpGetUtils(Context context, CallBack callBack, String url, Handler handler) {
		mContext=context;
		mURL = url;
		mCallBack = callBack;
		mHandler = handler;
	}

	public void run() {
		HttpURLConnection connection = null;
		BufferedReader bufferedReader = null;
		InputStream inputStream = null;
		try {
			URL url = new URL(mURL);
			connection = (HttpURLConnection) url.openConnection();
			// 设置请求方法，默认是GET
			connection.setRequestMethod("GET");
			connection.setDoInput(true);
			connection.setReadTimeout(5000);
			connection.setConnectTimeout(5000);
			connection.setUseCaches(true);
			if(Build.SERIAL!=null&&!Build.SERIAL.equals("")){
				connection.addRequestProperty("device-sn", Build.SERIAL);
//				connection.addRequestProperty("device-sn", "CNDFPBP9C161203001719");
			}else{
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						ToastUtils.showToast(mContext, mContext.getString(R.string.sn_null));
					}
				});
				return;
			}
			connection.addRequestProperty("appsotre_device_token",
					"");
			if(ApplicationUtils.getmBROKER()!=null&&!ApplicationUtils.getmBROKER().equals("")) {
				connection.setRequestProperty("device-broker",
						ApplicationUtils.getmBROKER());
			}else{
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						ToastUtils.showToast(mContext, mContext.getString(R.string.broker_null));
					}
				});
				return;
			}
			if(ApplicationUtils.getUUID()!=null&&!ApplicationUtils.getUUID().equals("")) {
				connection.addRequestProperty("deivce-uuid",
						ApplicationUtils.getUUID());
//				connection.setRequestProperty("deivce-uuid",
//						"052a9123e3a038d675d79e1a922b4be2c205d64725bbe1e736a0b95c42fe9923b41351244ab74fc14708d9194e8f2859");
			}else{
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						ToastUtils.showToast(mContext, mContext.getString(R.string.uuid_null));
					}
				});
				return;
			}
			if(ApplicationUtils.getmMODEL()!=null&&!ApplicationUtils.getmMODEL().equals("")) {
				connection.setRequestProperty("device-model", ApplicationUtils.getmMODEL());
			}else{
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						ToastUtils.showToast(mContext, mContext.getString(R.string.model_null));
					}
				});
				return;
			}
			if(ApplicationUtils.getmVERSION()!=null&&!ApplicationUtils.getmVERSION().equals("")) {
				connection.setRequestProperty("device-model-version", ApplicationUtils.getmVERSION());
			}else{
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						ToastUtils.showToast(mContext, mContext.getString(R.string.model_version_null));
					}
				});
				return;
			}
			connection.setRequestProperty("device-build-display",Build.DISPLAY);
			final int code = connection.getResponseCode();
			LogUtils.w(TAG, mURL+":"+code);
			if (code == 200) {
				inputStream = connection.getInputStream();
				bufferedReader = new BufferedReader(new InputStreamReader(
						inputStream,"utf-8"));
				StringBuffer stringBuffer = new StringBuffer();
				String valueString;
				while ((valueString = bufferedReader.readLine()) != null) {
					stringBuffer.append(valueString);
				}
				final String sendString = stringBuffer.toString();
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mCallBack.setResponseData(sendString.toString());
					}
				});
			} else {
				mHandler.post(new Runnable() {
					@Override
					public void run() {
						mCallBack.setFailedResponse(String.valueOf(code));
					}
				});
			}
		} catch (final Exception e) {
			e.printStackTrace();
			mHandler.post(new Runnable() {
				@Override
				public void run() {
					if (e instanceof EOFException) {//抛出此类异常，表示连接丢失，也就是说网络连接的另一端非正常关闭连接
						mCallBack.setTimeoutResponse(e.toString());
					} else if (e instanceof ConnectException) {//抛出此类异常，表示无法连接，也就是说当前主机不存在
						mCallBack.setTimeoutResponse(e.toString());
					} else if (e instanceof SocketException) {//抛出此类异常，表示连接正常关闭，也就是说另一端主动关闭连接
						mCallBack.setTimeoutResponse(e.toString());
					} else if (e instanceof BindException) {//抛出此类异常，表示端口已经被占用。
						mCallBack.setTimeoutResponse(e.toString());
					} else{
						mCallBack.setFailedResponse(e.toString());
					}
				}
			});
		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (bufferedReader != null) {
				try {
					bufferedReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (connection != null) {
				try {
					connection.disconnect();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	};
}
