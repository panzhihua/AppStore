package com.rongyan.appstore.utils;

public class AppInstallItem {
	
	private int notification_id;
	
	private String received_at;
	
	private String fail_reason;
	
	private String app_no;
	
	private String apk_no;
	
	private String package_name; // 软件包名称
	
	private int version_code; // 版本号
	
	private String version_name; // 版本名称
	
	private int old_version_code; // 机器上已经安装的版本号
	
	private String old_version_name;// 机器上已经安装的版本名称
	
	private String downloaded_at; // 下载完成时间
	
	private String installed_at;// 安装应用时间

	public int getNotification_id() {
		return notification_id;
	}

	public void setNotification_id(int notification_id) {
		this.notification_id = notification_id;
	}

	public String getReceived_at() {
		return received_at;
	}

	public void setReceived_at(String received_at) {
		this.received_at = received_at;
	}

	public String getFail_reason() {
		return fail_reason;
	}

	public void setFail_reason(String fail_reason) {
		this.fail_reason = fail_reason;
	}

	public String getApp_no() {
		return app_no;
	}

	public void setApp_no(String app_no) {
		this.app_no = app_no;
	}

	public String getApk_no() {
		return apk_no;
	}

	public void setApk_no(String apk_no) {
		this.apk_no = apk_no;
	}

	public String getPackage_name() {
		return package_name;
	}

	public void setPackage_name(String package_name) {
		this.package_name = package_name;
	}

	public int getVersion_code() {
		return version_code;
	}

	public void setVersion_code(int version_code) {
		this.version_code = version_code;
	}

	public String getVersion_name() {
		return version_name;
	}

	public void setVersion_name(String version_name) {
		this.version_name = version_name;
	}

	public int getOld_version_code() {
		return old_version_code;
	}

	public void setOld_version_code(int old_version_code) {
		this.old_version_code = old_version_code;
	}

	public String getOld_version_name() {
		return old_version_name;
	}

	public void setOld_version_name(String old_version_name) {
		this.old_version_name = old_version_name;
	}

	public String getDownloaded_at() {
		return downloaded_at;
	}

	public void setDownloaded_at(String downloaded_at) {
		this.downloaded_at = downloaded_at;
	}

	public String getInstalled_at() {
		return installed_at;
	}

	public void setInstalled_at(String installed_at) {
		this.installed_at = installed_at;
	}
	
}
