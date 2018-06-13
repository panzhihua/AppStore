package com.rongyan.appstore.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.rongyan.appstore.item.AppInfo;
import com.rongyan.appstore.utils.ApplicationUtils;
import com.rongyan.appstore.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DataBaseOpenHelper implements DataBaseOpenDao{
	
	private final static String TAG = "DataBaseOpenHelper";
	
	private Context mContext;
	
	private static DataBaseOpenHelper mDataBaseOpenHelper;
	
	public static DataBaseOpenHelper getInstance(Context context){
		if(mDataBaseOpenHelper == null){
			mDataBaseOpenHelper = new DataBaseOpenHelper(context);
		}
		return mDataBaseOpenHelper;
	}
	
	public DataBaseOpenHelper(Context context) {
		mContext = context;
	}

	@Override
	public boolean QueryBeingClass(int id,int page){
		if(mContext!=null) {
			Cursor query = mContext.getContentResolver().query(AppStoreProvider.CONTENT_URI, null,
					DatabaseColume.ClassInfo.ID + "=" + id + " and " + DatabaseColume.ClassInfo.PAGE + "=" + page, null, null);
			int count = query.getCount();
			if (query != null) {
				query.close();
			}
			if (count > 0) {
				return true;
			} else {
				return false;
			}
		}else{
			return false;
		}
	}

	@Override
	public boolean QueryOverdueClass(int id ,int page){
		if(mContext!=null) {
			Cursor query = mContext.getContentResolver().query(AppStoreProvider.CONTENT_URI, null,
					DatabaseColume.ClassInfo.ID + "=" + id + " and " + DatabaseColume.ClassInfo.PAGE + "=" + page, null, null);
			if (query.getCount() > 0) {
				while (query.moveToNext()) {
					int difference = StringUtils.getSystemTime() - query.getInt(query.getColumnIndex(DatabaseColume.ClassInfo.TIME));
					String value = query.getString(query.getColumnIndex(DatabaseColume.ClassInfo.VALUE));
					if (query != null) {
						query.close();
					}
					if (value.equals("") || value == null) {//内容为空也需要重新获取
						return true;
					}
					if (difference > ApplicationUtils.getCache_expires()* 60) {//大于24小时则认为已过期
						return true;
					} else {
						return false;
					}
				}
			}
			if (query != null) {
				query.close();
			}
		}
		return false;
	}

	@Override
	public void AddClass(final int id,final String value,final int page){
		try{
			Thread t = new Thread(new Runnable(){
				public void run(){
					ContentValues  bean = new ContentValues();
					bean.put(DatabaseColume.ClassInfo.ID, id);
					bean.put(DatabaseColume.ClassInfo.VALUE, value);
					bean.put(DatabaseColume.ClassInfo.PAGE, page);
					bean.put(DatabaseColume.ClassInfo.TIME,StringUtils.getSystemTime());
					mContext.getContentResolver().insert(AppStoreProvider.CONTENT_URI, bean);
				}});
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void UpdateClass(final int id,final String value,final int page){
		try{
			Thread t = new Thread(new Runnable(){
				public void run() {
					ContentValues values = new ContentValues();
					values.put("class_value", value);
					values.put("class_page", page);
					values.put("class_time", StringUtils.getSystemTime());
					mContext.getContentResolver().update(AppStoreProvider.CONTENT_URI, values, DatabaseColume.ClassInfo.ID + "=" + id, null);
				}});
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public List<String> QueryClass(int id){
		List<String> valueList=new ArrayList<>();
		if(mContext!=null) {
			Cursor query = mContext.getContentResolver().query(AppStoreProvider.CONTENT_URI, null,
					DatabaseColume.ClassInfo.ID + "=" + id, null, DatabaseColume.ClassInfo.PAGE + " ASC");
			if (query.getCount() > 0) {
				while (query.moveToNext()) {
					String value = query.getString(query.getColumnIndex(DatabaseColume.ClassInfo.VALUE));
					valueList.add(value);
				}
			}
			if (query != null) {
				query.close();
			}
		}
		return valueList;
	}

	@Override
	public void DeleteClass(final int id){
		try{
			Thread t = new Thread(new Runnable(){
				public void run() {
					mContext.getContentResolver().delete(AppStoreProvider.CONTENT_URI, DatabaseColume.ClassInfo.ID+"=?", new String[] { id+"" });
				}});
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public boolean QueryBeingApp(String app_no){
		if(mContext!=null) {
			Cursor query = mContext.getContentResolver().query(AppStoreProvider.APP_URI, null,
					DatabaseColume.AppInfo.ID + "='" + app_no+"'", null, null);
			int count = query.getCount();
			if (query != null) {
				query.close();
			}
			if (count > 0) {
				return true;
			} else {
				return false;
			}
		}else{
			return false;
		}
	}

	@Override
	public void AddApp(final AppInfo appinfo){
		try{
			Thread t = new Thread(new Runnable(){
				public void run() {
					ContentValues  bean = new ContentValues();
					bean.put(DatabaseColume.AppInfo.ID, appinfo.getApp_no());
					bean.put(DatabaseColume.AppInfo.TIMES, appinfo.getInstalled_times());
					bean.put(DatabaseColume.AppInfo.RATINGS, appinfo.getRatings());
					bean.put(DatabaseColume.AppInfo.INSTALLING, appinfo.getRatings());
					mContext.getContentResolver().insert(AppStoreProvider.APP_URI, bean);
				}});
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void UpdateApp(final AppInfo appinfo){
		try{
			Thread t = new Thread(new Runnable(){
				public void run() {
					ContentValues values = new ContentValues();
					values.put("ratings",appinfo.getRatings());
					values.put("installed_times",appinfo.getInstalled_times());
					mContext.getContentResolver().update(AppStoreProvider.APP_URI, values, DatabaseColume.AppInfo.ID+"='"+appinfo.getApp_no()+"'", null);
			}});
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void UpdateAppRatings(final AppInfo appinfo){
		try{
			Thread t = new Thread(new Runnable(){
				public void run() {
					ContentValues values = new ContentValues();
					values.put("ratings",appinfo.getRatings());
					mContext.getContentResolver().update(AppStoreProvider.APP_URI, values, DatabaseColume.AppInfo.ID+"='"+appinfo.getApp_no()+"'", null);
				}});
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void UpdateAppTnstalledTimes(final AppInfo appinfo){
		try{
			Thread t = new Thread(new Runnable(){
				public void run() {
					ContentValues values = new ContentValues();
					values.put("installed_times",appinfo.getInstalled_times());
					mContext.getContentResolver().update(AppStoreProvider.APP_URI, values, DatabaseColume.AppInfo.ID+"='"+appinfo.getApp_no()+"'", null);
				}});
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public void UpdateAppInstalling(final AppInfo appinfo){
		try{
			Thread t = new Thread(new Runnable(){
				public void run() {
					ContentValues values = new ContentValues();
					values.put("installing",appinfo.getInstalling());
					mContext.getContentResolver().update(AppStoreProvider.APP_URI, values, DatabaseColume.AppInfo.ID+"='"+appinfo.getApp_no()+"'", null);
				}});
			t.start();
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Override
	public AppInfo QueryApp(String app_no){
		if(mContext!=null) {
			Cursor query = mContext.getContentResolver().query(AppStoreProvider.APP_URI, null,
					DatabaseColume.AppInfo.ID + "='" + app_no+"'", null, null);
			if (query.getCount() > 0) {
				while (query.moveToNext()) {
					int ratings = query.getInt(query.getColumnIndex(DatabaseColume.AppInfo.RATINGS));
					int installing = query.getInt(query.getColumnIndex(DatabaseColume.AppInfo.INSTALLING));
					String times = query.getString(query.getColumnIndex(DatabaseColume.AppInfo.TIMES));
					AppInfo mAppInfo=new AppInfo(app_no,times,ratings,installing);
					return mAppInfo;
				}
			}
			if (query != null) {
				query.close();
			}
		}
		return null;
	}

	@Override
	public List<AppInfo> GetAllApp(){
		List<AppInfo> valueList=new ArrayList<>();
		if(mContext!=null) {
			Cursor query = mContext.getContentResolver().query(AppStoreProvider.APP_URI, null,
					null, null, null);
			if (query.getCount() > 0) {
				while (query.moveToNext()) {
					String app_no = query.getString(query.getColumnIndex(DatabaseColume.AppInfo.ID));
					int ratings = query.getInt(query.getColumnIndex(DatabaseColume.AppInfo.RATINGS));
					int installing = query.getInt(query.getColumnIndex(DatabaseColume.AppInfo.INSTALLING));
					String times = query.getString(query.getColumnIndex(DatabaseColume.AppInfo.TIMES));
					AppInfo mAppInfo=new AppInfo(app_no,times,ratings,installing);
					valueList.add(mAppInfo);
				}
			}
			if (query != null) {
				query.close();
			}
		}
		return valueList;
	}
}
