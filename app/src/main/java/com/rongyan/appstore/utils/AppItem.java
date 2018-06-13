package com.rongyan.appstore.utils;

public class AppItem {
	
	private String package_ = null;

	private String version;
	
	private int versionCode;
	
	private int useRate = 0;
	
	public String getPackage_() {
		return package_;
	}
	
	public void setPackage_(String package_) {
		this.package_ = package_;
	}
	
	public String getVersion() {
		return version;
	}
	
	public void setVersion(String version) {
		this.version = version;
	}
	
	public int getUseRate() {
		return useRate;
	}
	
	public void setUseRate(int useRate) {
		this.useRate = useRate;
	}

	public int getVersionCode() {
		return versionCode;
	}

	public void setVersionCode(int versionCode) {
		this.versionCode = versionCode;
	}
	
}
