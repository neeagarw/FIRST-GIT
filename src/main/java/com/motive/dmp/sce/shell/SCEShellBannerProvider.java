package com.motive.dmp.sce.shell;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.shell.plugin.support.DefaultBannerProvider;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class SCEShellBannerProvider extends DefaultBannerProvider {

	@Value("#{versionInfo['version']}")
	private String version;

	@Value("#{versionInfo['build.date']}")
	private String buildDate;

	public String getBanner() {
		StringBuffer buf = new StringBuffer();
		buf.append("======================================================" + OsUtils.LINE_SEPARATOR);
		buf.append("*                    SCE Shell                       *" + OsUtils.LINE_SEPARATOR);
		buf.append("======================================================" + OsUtils.LINE_SEPARATOR);
		buf.append("Version:" + this.getVersion() + "  BuildDate:" + buildDate);
		return buf.toString();
	}

	public String getVersion() {
		return version;
	}

	public String getWelcomeMessage() {
		return "Welcome to SCE CLI";
	}

	@Override
	public String getProviderName() {
		return "SCE-Shell Banner";
	}
}