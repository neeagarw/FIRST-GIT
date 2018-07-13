package com.motive.dmp.sce;

public class SCEConfig {

	private String baseUrl;

	private String username;

	private String password;

	public SCEConfig() {
	}

	public SCEConfig(String baseUrl, String username, String password) {
		this.baseUrl = baseUrl;
		this.username = username;
		this.password = password;
	}

	public String getBaseUrl() {
		return baseUrl.trim().replaceAll("(.+)/$", "$1") + "/sce";
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}

	@Override
	public String toString() {
		return "SCEConfig [baseUrl=" + baseUrl + ", username=" + username
				+ ", password=" + password + "]";
	}

}
