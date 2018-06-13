package com.rongyan.appstore.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * 数据库初始化类
 */

public class AppStoreOpenHelper extends SQLiteOpenHelper {

    private final static String TAG = "AppStoreOpenHelper";

    private SQLiteDatabase mDB = null;

    public static final int DB_VERSION = 1;

    private final static String classSql = "CREATE TABLE " + DatabaseColume.ClassInfo.TABLENAME + "("
            + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DatabaseColume.ClassInfo.ID + " INTEGER,"
            + DatabaseColume.ClassInfo.VALUE + " TEXT,"
            + DatabaseColume.ClassInfo.PAGE + " INTEGER,"
            + DatabaseColume.ClassInfo.TIME + " INTEGER)";

    private final static String appSql = "CREATE TABLE " + DatabaseColume.AppInfo.TABLENAME + "("
            + "id" + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DatabaseColume.AppInfo.ID + " TEXT,"
            + DatabaseColume.AppInfo.TIMES + " TEXT,"
            + DatabaseColume.AppInfo.RATINGS + " INTEGER,"
            + DatabaseColume.AppInfo.INSTALLING + " INTEGER)";

    public AppStoreOpenHelper(Context context){
        super(context, DatabaseColume.DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        mDB = db;
        mDB.execSQL(classSql);
        mDB.execSQL(appSql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        // 升级
        mDB = db;
        if(oldVersion<newVersion){

        }
    }
}
