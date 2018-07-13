package com.motive.dmp.sce;

import static org.hamcrest.Matchers.*;
import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Strings;
import com.motive.dcm.nbi.client.ConfigurationClient;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-test.xml" })
public class SCEClientImplIntegrationTest {

	Logger logger = LoggerFactory.getLogger(SCEClientImplIntegrationTest.class);

	@Autowired
	ClientBuilder builder;

	private SCEClientHTMLScraperImpl client;

	private ConfigurationClient nbiClient;

	@Before
	public void setUp() throws IOException, GeneralSecurityException {

		System.setProperty("config", "src/test/resources/config.properties");

		Properties props = new Properties();
		props.load(new FileInputStream(new File(System.getProperty("config"))));

		String proxyHost = props.getProperty("proxyHost");
		String proxyPort = props.getProperty("proxyPort");
		if (Strings.emptyToNull(proxyHost) != null
				&& Strings.emptyToNull(proxyPort) != null) {

			System.setProperty("http.proxyHost", proxyHost);
			System.setProperty("http.proxyPort", proxyPort);
			System.setProperty("proxyHost", proxyHost);
			System.setProperty("proxyPort", proxyPort);

		}

		boolean trustAllCerts = Boolean.parseBoolean(props
				.getProperty("trustAllCerts"));
		
		if (trustAllCerts) {
			SSLConfigurer.trustAllCerts();
		}

		client = (SCEClientHTMLScraperImpl) builder.getSCEClient();
		nbiClient = builder.getNBIClient();
	}

	@After
	public void tearDown() {
		client.logout();
	}

	@Test
	public void auth() {

		client.login();
		boolean authenticated = client.isAuthenticated();
		logger.debug("is authenticated now: {}", authenticated);
		assertThat(authenticated, is(true));
		client.logout();

		authenticated = client.isAuthenticated();
		logger.debug("is authenticated now: {}", authenticated);
		assertThat(authenticated, is(false));
	}

	@Test
	public void get() throws RemoteException {
		client.login();
		ConfigurationItem item = client.get("dcm.compressor.type");
		assertThat(item.getValue(), is("snappy"));
		assertThat(item.isReadOnly(), is(true));
	}

	@Test
	public void create() throws RemoteException {
		client.delete("test");
		client.createOrUpdate("test", "testValue");

		String val = nbiClient.getConfigurationProperty("test");
		assertNotNull(val);
		assertThat(val, is("testValue"));

	}

	@Test
	public void update() throws RemoteException {
		client.delete("test");

		String val = nbiClient.getConfigurationProperty("test");
		assertNull(val);

		client.createOrUpdate("test", "testValue");
		val = nbiClient.getConfigurationProperty("test");
		assertThat(val, is("testValue"));

		client.createOrUpdate("test", "testValue2");

		val = nbiClient.getConfigurationProperty("test");
		assertThat(val, is("testValue2"));

	}

	@Test
	public void delete() throws RemoteException {
		client.delete("test");
		String val = nbiClient.getConfigurationProperty("test");
		assertNull(val);

	}

}
