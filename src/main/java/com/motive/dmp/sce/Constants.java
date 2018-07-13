package com.motive.dmp.sce;

public interface Constants {

	public static final String MAIN_VIEW = "selectConfigurationView.do";

	public static final String LOGIN_URL = "j_security_check";

	public static final String LOGOUT_URL = "logout.udo";

	public static final String DELETE_URL = "deleteConfigurationItems.do";

	public static final String CREATE_URL = "createConfigurationItem.do";

	public static final String UPDATE_URL = "editConfigurationItem.do";

	public static final String GLOBAL_SERVER_NAME = "GLOBAL_CONFIGURATION";

	public static final String[] CSRF_TOKEN_REQUIRED_URLS = new String[] {
			LOGOUT_URL, DELETE_URL, CREATE_URL, UPDATE_URL };

}