package com.motive.dmp.sce;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.motive.dcm.nbi.client.ClientFactory;
import com.motive.dcm.nbi.client.ConfigurationClient;
import com.motive.dcm.nbi.client.NbiConfig;

@Component
public class ClientBuilder {

	@Autowired
	private ClientFactory clientFactory;

	public void configure() throws FileNotFoundException, IOException,
			GeneralSecurityException {
		Properties config = getProperties();

		if (Boolean.parseBoolean(config.getProperty("trustAllCerts"))) {
			SSLConfigurer.trustAllCerts();
		}

	}

	public SCEClient getSCEClient() throws IOException {

		Properties config = getProperties();

		SCEClientHTMLScraperImpl client = new SCEClientHTMLScraperImpl();
		client.setSCEConfig(new SCEConfig(config.getProperty("baseurl"), config
				.getProperty("sce.username"), config
				.getProperty("sce.password")));
		client.setConfigurationClient(getNBIClient());
		return client;

	}

	public ConfigurationClient getNBIClient() throws IOException {
		Properties config = getProperties();
		NbiConfig nbiConfig = new NbiConfig();
		nbiConfig.setBaseUrl(config.getProperty("baseurl").trim()
				.replaceAll("(.+)/$", "$1")
				+ "/nbi");
		nbiConfig.setUsername(config.getProperty("nbi.username"));
		nbiConfig.setPassword(config.getProperty("nbi.password"));
		return clientFactory.getConfigurationClient(nbiConfig);
	}

	public Properties getProperties() throws FileNotFoundException,
			IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(new File(System.getProperty("config"))));
		return props;
	}
}