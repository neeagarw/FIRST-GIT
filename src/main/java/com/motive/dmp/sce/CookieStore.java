package com.motive.dmp.sce;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.NewCookie;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CookieStore {

	private static final Logger logger = LoggerFactory
			.getLogger(CookieStore.class);

	private List<NewCookie> cookies = new ArrayList<NewCookie>();

	public void addCookie(NewCookie cookie) {
		logger.debug("Adding cookie: {}", cookie);
		if (contains(cookie)) {
			remove(cookie);
		}
		cookies.add(cookie);
	}

	public void remove(NewCookie cookie) {
		for (int i = 0; i < cookies.size(); i++) {
			if (cookie.getName().equals(cookies.get(i).getName())) {
				cookies.remove(i);
			}
		}
	}

	public void processCookies(List<NewCookie> cookies) {
		for (NewCookie c : cookies) {
			if (c.getMaxAge() == 0 || c.getValue().isEmpty()) {
				logger.debug("Removing invalidated cookie: {}", c);
				remove(c);
			} else {
				addCookie(c);
			}
		}
	}

	public List<Object> getCookies() {
		return Arrays.asList(cookies.toArray());
	}

	public boolean contains(NewCookie cookie) {
		for (NewCookie c : cookies) {
			if (c.getName().equals(cookie.getName())) {
				return true;
			}
		}
		return false;
	}

}
