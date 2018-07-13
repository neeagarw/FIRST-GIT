package com.motive.dmp.sce.shell;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultHistoryFileNameProvider;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SCEShellHistoryFileNameProvider extends DefaultHistoryFileNameProvider {

	public String getHistoryFileName() {
		return System.getProperty("user.home") + "/.sce-shell.history";
	}

	@Override
	public String getProviderName() {
		return "SCE Shell history file name provider";
	}

}
