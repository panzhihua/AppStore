package com.rongyan.appstore.utils;

public class AppUninstallItem {
	
	private int notification_id;// 消息 id，回传
	
	private String received_at; // 接收到的时间
	
	private String uninstalled_at; // 卸载的时间
	
	private String package_name; // 卸载的报名
	
	private int version_code; // 卸载的版本号
	
	private String version_name; // 卸载的版本名称

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

	public String getUninstalled_at() {
		return uninstalled_at;
	}

	public void setUninstalled_at(String uninstalled_at) {
		this.uninstalled_at = uninstalled_at;
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
	
}
