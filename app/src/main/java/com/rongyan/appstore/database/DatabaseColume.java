package com.rongyan.appstore.database;

import java.io.Serializable;

public class DatabaseColume {
	
	public static final String DB_NAME = "appstore.db";//数据库名称

	public static final int UPDATEID=10000;//更新ID

	public static final int HOTID=10001;//最热ID

	public static final int NEWID=10002;//最新ID

	public static final int BANNERID=10003;//BannerID

	public static final int CLASSID=10004;//类别ID

	public static final int PAGE=1;
	
	public static class ClassInfo implements Serializable {

		public static final String TABLENAME = "classinfo";//表名
		
		public static final String ID = "class_id";//类别id
		
		public static final String VALUE = "class_value";//类别内容

		public static final String PAGE = "class_page";//类别内容页码
		
		public static final String TIME = "class_time";//类别获取时间(用于判断是否需要重新获取)
	}

	public static class AppInfo implements Serializable {

		public static final String TABLENAME = "appinfo";//表名

		public static final String ID = "app_no";//app唯一标识

		public static final String TIMES = "installed_times";//安装次数

		public static final String RATINGS = "ratings";//评分

		public static final String INSTALLING = "installing";//是否正在安装中(0正常状态，1安装中，2下载中)
	}

}
