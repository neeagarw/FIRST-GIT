package com.motive.dmp.sce;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status.Family;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Strings;
import com.motive.dcm.nbi.client.ConfigurationClient;
import com.motive.dcm.nbi.model.ConfigurationProperty;
import com.motive.dmp.sce.exception.AuthenticationError;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientHandlerException;
import com.sun.jersey.api.client.ClientRequest;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.ClientResponse.Status;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.filter.ClientFilter;
import com.sun.jersey.core.util.MultivaluedMapImpl;

@Component
@Lazy
@Scope(value = ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SCEClientHTMLScraperImpl implements SCEClient {

	private static final Logger logger = LoggerFactory.getLogger(SCEClientHTMLScraperImpl.class);

	private SCEConfig config;

	private CookieStore cookieStore = new CookieStore();

	private ConfigurationClient nbiClient;

	private Client client;

	public SCEClientHTMLScraperImpl() {
	}

	public void setSCEConfig(SCEConfig config) {
		this.config = config;
		configure();
	}

	public void setConfigurationClient(ConfigurationClient nbiClient) {
		this.nbiClient = nbiClient;
	}

	private void configure() {

		client = Client.create();
		client.getProperties().put(ClientConfig.PROPERTY_FOLLOW_REDIRECTS, false);
		client.addFilter(new ClientFilter() {

			@Override
			public ClientResponse handle(ClientRequest request) throws ClientHandlerException {

				if (cookieStore.getCookies().size() > 0) {
					request.getHeaders().put("Cookie", cookieStore.getCookies());
					request.getHeaders().put("Referer",
							Arrays.asList(new Object[] { config.getBaseUrl() + Constants.DELETE_URL }));
				}

				logger.trace("request: {}", request.getHeaders());

				ClientResponse response = getNext().handle(request);
				if (response.getCookies() != null) {
					// simple addAll just for illustration (should probably
					// check for duplicates and expired cookies)
					cookieStore.processCookies(response.getCookies());
				}

				logger.trace("response: {}", response);

				return response;
			}
		});
	}

	private WebResource getBaseWebResource(String path) {

		WebResource webResource = client.resource(config.getBaseUrl());

		if (path != null) {
			webResource = webResource.path(path);
		}

		if (csrfGuardEnabled && csrfToken != null) {
			path = path.replaceAll("^/", "").replaceAll("/$", "");
			for (String url : Constants.CSRF_TOKEN_REQUIRED_URLS) {
				if (path.equals(url)) {
					webResource = webResource.queryParam("DMP_CSRFTOKEN", csrfToken);
				}
			}
		}

		return webResource;
	}

	boolean isAuthenticated() {

		ClientResponse response = getBaseWebResource(Constants.MAIN_VIEW).head();
		Status status = Status.fromStatusCode(response.getStatus());
		logger.debug("Status {}, {}", status.getStatusCode(), status.getReasonPhrase());

		Family statusFamily = Status.getFamilyByStatusCode(response.getStatus());

		if (statusFamily == Family.SUCCESSFUL) {
			logger.debug("already authenticated");
			return true;
		} else if (statusFamily == Family.REDIRECTION) {
			List<String> headers = response.getHeaders().get("Location");
			for (String location : headers) {
				logger.debug("Redirect being requested, new location: {}", location);
				if (location.indexOf("login.vm") > 0) {
					return false;
				}
			}
		}

		throw new IllegalStateException("Server did not return expected reponse, unable to determine login state");

	}

	@Override
	public boolean login() {
		logger.debug("login - entering");
		boolean authenticated = isAuthenticated();
		logger.debug("is authenticated: {}", authenticated);
		if (!authenticated) {
			MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
			formData.add("j_username", config.getUsername());
			formData.add("j_password", config.getPassword());
			formData.add("login", "Log On");
			ClientResponse response = getBaseWebResource(Constants.LOGIN_URL).type(
					MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);

			Status status = Status.fromStatusCode(response.getStatus());
			if (status == Status.FORBIDDEN) {
				return false;
			}
			handleCsrfGuard();
		}
		return true;
	}

	@Override
	public void logout() {
		logger.info("Logging out");
		boolean authenticated = isAuthenticated();
		logger.debug("is authenticated: {}", authenticated);
		if (authenticated) {
			ClientResponse response = getBaseWebResource(Constants.LOGOUT_URL).get(ClientResponse.class);
			List<String> headers = response.getHeaders().get("Location");
			for (String location : headers) {
				logger.debug("Redirect {}", location);
				if (location.indexOf("login.vm") > 0) {
					logger.info("Successfully logged out");
				}
			}
		} else {
			logger.info("Not logged on");
		}
	}

	@Override
	public Map<String, ConfigurationItem> getAll() throws RemoteException {

		authenticate();

		ClientResponse response = client.resource(config.getBaseUrl()).path(Constants.MAIN_VIEW)
				.get(ClientResponse.class);

		String body = response.getEntity(String.class);
		Document doc = Jsoup.parse(body);

		Iterator<Element> it = doc.getElementsByAttributeValue("name", "configurationItemIDs").iterator();

		Set<ConfigurationItem> allItems = new TreeSet<ConfigurationItem>();

		while (it.hasNext()) {
			Element e = it.next();
			ConfigurationItem item = new ConfigurationItem();
			item.id = Long.parseLong(e.val());
			if (doc.getElementById(e.val()) != null) {
				item.value = doc.getElementById(e.val()).text().trim();
			} else {
				item.encrypted = true;
			}
			String regex = ".+showEditParameterDialog\\('" + e.val() + "'.*,.*'(.+)'.*,\\s*(.+)\\).*";
			Pattern p = Pattern.compile(regex);

			Elements a = doc.getElementsByAttributeValueMatching("href", p);
			if (a.size() > 0) {
				Matcher m = p.matcher(a.attr("href"));
				if (m.find()) {
					item.key = m.group(1).trim();
				}
			} else {
				Element td = e.parent().parent().getElementsByIndexEquals(2).first();
				for (Node n : td.textNodes()) {
					if (n.outerHtml().trim().length() > 0) {
						item.key = n.outerHtml().trim();
						item.readOnly = true;
						break;
					}
				}
			}
			allItems.add(item);
		}

		ConfigurationProperty[] props = nbiClient.getAllConfigurationProperties();
		for (ConfigurationItem item : allItems) {
			if (item.isEncrypted()) {
				for (ConfigurationProperty p : props) {
					if (item.getKey().equals(p.getName())) {
						item.value = p.getValue();
						break;
					}
				}
			}
		}

		Map<String, ConfigurationItem> allItemsMap = new LinkedHashMap<String, ConfigurationItem>();
		for (ConfigurationItem item : allItems) {
			allItemsMap.put(item.getKey(), item);
		}

		return allItemsMap;
	}

	private boolean csrfGuardChecked = false;
	private boolean csrfGuardEnabled = false;
	private String csrfToken;

	private void handleCsrfGuard() {

		if (csrfGuardChecked) {
			return;
		}

		ClientResponse response = client.resource(config.getBaseUrl()).path(Constants.MAIN_VIEW)
				.get(ClientResponse.class);

		String body = response.getEntity(String.class);
		Document doc = Jsoup.parse(body);

		Iterator<Element> scripts = doc.getElementsByTag("script").iterator();
		while (scripts.hasNext()) {
			Element tag = scripts.next();
			if (tag.attr("src").contains("CsrfScriptServlet")) {

				logger.info("CSRF protection is enabled, attempting to get a token");

				response = client.resource(config.getBaseUrl()).path("CsrfScriptServlet")
						.queryParam("FETCH-CSRF-TOKEN", "1").header("FETCH-CSRF-TOKEN", "1").post(ClientResponse.class);
				String token = response.getEntity(String.class);
				logger.info("CsrfScriptServlet fetched: {}", token);
				if (token.contains(":")) {
					csrfToken = token.substring(token.indexOf(":") + 1);
					csrfGuardEnabled = true;
					break;
				}
			}
		}
		csrfGuardChecked = true;

	}

	private void authenticate() {
		if (isAuthenticated()) {
			return;
		}
		logger.info("Logging in");
		if (login()) {
			logger.info("Logged in");
		} else {
			throw new AuthenticationError("Failed to log on to SCE");
		}
	}

	@Override
	public void createOrUpdate(String key, String value) throws RemoteException {
		logger.info("Create or update SCE: {}", key);

		Map<String, ConfigurationItem> allItems = getAll();

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("serverName", Constants.GLOBAL_SERVER_NAME);
		formData.add("name", key);
		formData.add("value", value);
		formData.add("scrollTop", "0");

		String url = null;

		boolean exists = allItems.containsKey(key);
		if (exists) {
			// update
			if (allItems.get(key).isReadOnly()) {
				throw new IllegalArgumentException("The SCE [" + key + "]is read only and should not be updated!");
			}

			String id = Long.toString(allItems.get(key).getId());
			logger.info("The specified key [{}] already exists as ID: {} - updating it", key, id);
			formData.add("configurationItemID", id);
			url = Constants.UPDATE_URL;
		} else {
			// create
			logger.info("The specified key [{}] does not exist - creating it", key);
			url = Constants.CREATE_URL;
		}

		getBaseWebResource(url).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(ClientResponse.class, formData);
	}

	@Override
	public void delete(String key) throws RemoteException {

		logger.info("Delete SCE: {}", key);

		Map<String, ConfigurationItem> allItems = getAll();
		if (!allItems.containsKey(key)) {
			logger.warn("The specified key [{}] does not exist!", key);
			return;
		}

		if (allItems.get(key).isReadOnly()) {
			throw new IllegalArgumentException("The SCE [" + key + "]is read only and should not be updated!");
		}

		logger.debug("Item to delete: {}", allItems.get(key));

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("serverName", Constants.GLOBAL_SERVER_NAME);
		formData.add("configurationItemIDs", Long.toString(allItems.get(key).getId()));

		logger.info("ConfigurationItem ID: {}", formData.get("configurationItemIDs"));

		getBaseWebResource(Constants.DELETE_URL).type(MediaType.APPLICATION_FORM_URLENCODED_TYPE).post(
				ClientResponse.class, formData);

	}

	@Override
	public ConfigurationItem get(String key) throws RemoteException {
		Map<String, ConfigurationItem> allItems = getAll();
		if (allItems != null && Strings.emptyToNull(key) != null) {
			ConfigurationItem item = allItems.get(key);
			logger.debug("Item retrieved: {}", item);
			return item;
		}
		return null;
	}

}
