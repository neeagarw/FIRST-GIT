package com.motive.dmp.sce.shell;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.shell.core.CommandMarker;
import org.springframework.shell.core.annotation.CliAvailabilityIndicator;
import org.springframework.shell.core.annotation.CliCommand;
import org.springframework.shell.core.annotation.CliOption;
import org.springframework.shell.support.util.OsUtils;
import org.springframework.stereotype.Component;
import org.webjars.diff_match_patch;
import org.webjars.diff_match_patch.Diff;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.io.Files;
import com.motive.dcm.nbi.beans.ConfigurationPropertyBean;
import com.motive.dcm.nbi.client.ClientFactory;
import com.motive.dcm.nbi.client.ConfigurationClient;
import com.motive.dcm.nbi.dto.ConfigurationProperties;
import com.motive.dcm.nbi.model.ConfigurationProperty;
import com.motive.dmp.sce.ClientBuilder;
import com.motive.dmp.sce.ConfigurationItem;
import com.motive.dmp.sce.DiffReport;
import com.motive.dmp.sce.DiffReportItem;
import com.motive.dmp.sce.DiffReportItem.Operation;
import com.motive.dmp.sce.SCEClient;
import com.motive.dmp.sce.XmlUtils;
import com.motive.dmp.sce.model.SCECliConfigurationProperties;
import com.motive.dmp.sce.model.SCECliConfigurationPropertyBean;

@Component
public class SCECommands implements CommandMarker {

	private static final Logger logger = LoggerFactory.getLogger(SCECommands.class);

	@CliAvailabilityIndicator({ "config" })
	public boolean isConfigAvailable() {
		return true;
	}

	@CliAvailabilityIndicator({ "export", "get", "set", "delete", "import", "logout" })
	public boolean isOtherAvailable() {
		if (configClient != null && sceClient != null) {
			return true;
		} else {
			return false;
		}
	}

	@Autowired
	private ClientBuilder builder;

	@Autowired
	private ClientFactory nbiClientFactory;

	private ConfigurationClient configClient;

	private SCEClient sceClient;

	private diff_match_patch diffLib = new diff_match_patch();

	@CliCommand(value = "init", help = "Initialize SCE client")
	public String init(
			@CliOption(key = { "", "configFile" }, mandatory = true, help = "Configuration file") final File configFile)
			throws IOException, GeneralSecurityException {

		// Set path to system properties
		System.setProperty("config", configFile.getCanonicalPath());

		// Build clients
		builder.configure();
		configClient = builder.getNBIClient();
		sceClient = builder.getSCEClient();

		return "Configuration set";

	}

	@CliCommand(value = "export", help = "Export all SCE properties")
	public String export(
			@CliOption(key = { "", "target" }, mandatory = true, help = "Target directory or file to save exported properties and XML files") final File targetDir,
			@CliOption(key = { "", "externalizeXml" }, mandatory = false, help = "Determine whether XML properties should be externalized", unspecifiedDefaultValue = "false") final boolean externalizeXml)
			throws IOException {

		if (externalizeXml && targetDir.isFile()) {
			return "You must specify a directory when externalizeXml is activated";
		}

		if (externalizeXml && !targetDir.exists()) {
			targetDir.mkdirs();
		}

		String path = targetDir.getCanonicalPath();
		File targetFile = new File(path + "/sce.xml");
		if (!externalizeXml && !targetDir.isDirectory()) {
			targetFile = targetDir;
		}
		logger.info("Using {} as target file path", targetFile.getCanonicalFile());

		boolean success = false;
		try {
			ConfigurationProperty[] props = configClient.getAllConfigurationProperties();

			if (externalizeXml) {
				for (ConfigurationProperty p : props) {
					if (p == null) {
						logger.error("How can i be null here");
					}

					boolean xmlProp = isXmlProperty(p.getName());
					logger.debug("{} - is XML property: {}", p.getName(), xmlProp);

					if (xmlProp) {
						String formatted = p.getValue();
						try {
							formatted = XmlUtils.prettyPrintXml(XmlUtils.stripCDATA(p.getValue()));

							logger.debug("key: {} - value: {}", p.getName(), formatted);

							String filename = p.getName() + ((p.getName().endsWith(".xml")) ? "" : ".xml");

							p.setValue("${" + filename + "}");

							File file = new File(path + "/" + filename);

							FileUtils.writeStringToFile(file, formatted);

							logger.debug("saved to file: {}", file.getCanonicalFile());
						} catch (Throwable t) {
							logger.warn("Faield to format a property to XML: " + p.getName(), t);
						}
					}
				}
			}
			Arrays.sort(props);
			ConfigurationProperties allProps = new ConfigurationProperties(props);

			JAXBContext jaxbContext = JAXBContext.newInstance(ConfigurationProperties.class);
			Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			// output pretty printed
			jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			jaxbMarshaller.marshal(allProps, targetFile);

			success = true;

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (JAXBException e) {
			e.printStackTrace();
		}
		if (success) {
			return "Exported SCE properties to: " + targetFile.getCanonicalPath();
		} else {
			return "Failed to export SCE properties";
		}
	}

	@CliCommand(value = "get", help = "Print SCE property value")
	public String get(
			@CliOption(key = { "", "name" }, mandatory = true, help = "SCE property name") final String name,
			@CliOption(key = { "regex" }, mandatory = false, help = "Enable regular expression", specifiedDefaultValue = "true", unspecifiedDefaultValue = "false") final boolean useRegExp)
			throws RemoteException {

		ConfigurationProperty[] properties;
		if (useRegExp) {
			properties = configClient.getConfigurationPropertiesByFilter(name);
		} else {
			ConfigurationPropertyBean bean = new ConfigurationPropertyBean(name,
					configClient.getConfigurationProperty(name));
			properties = new ConfigurationProperty[] { bean };
		}

		if (properties == null || properties.length == 0) {
			return "No property found";
		}

		StringBuilder output = new StringBuilder("Retrieved [" + properties.length + "] properties"
				+ OsUtils.LINE_SEPARATOR);

		int idx = 1;
		for (ConfigurationProperty p : properties) {
			output.append("------------------------------------------" + OsUtils.LINE_SEPARATOR);
			output.append("Key[" + idx + "]:  " + p.getName() + OsUtils.LINE_SEPARATOR);
			output.append("Value[" + idx + "]: " + p.getValue() + OsUtils.LINE_SEPARATOR);
			idx++;
		}
		return output.toString();
	}

	@CliCommand(value = "set", help = "Create or update SCE property")
	public String set(@CliOption(key = { "", "name" }, mandatory = true, help = "SCE property name") final String name,
			@CliOption(key = { "value" }, mandatory = true, help = "SCE property value") final String value) {

		boolean success = false;
		try {
			sceClient.createOrUpdate(name, value);
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success ? "Done" : "Failed";
	}

	@CliCommand(value = "delete", help = "Delete SCE property")
	public String delete(
			@CliOption(key = { "", "name" }, mandatory = true, help = "SCE property name") final String name) {

		boolean success = false;
		try {
			sceClient.delete(name);
			success = true;
		} catch (Exception e) {
			e.printStackTrace();
		}

		return success ? "Done" : "Failed";
	}

	@CliCommand(value = "import", help = "Import SCE properties from file")
	public String load(
			@CliOption(key = { "localFile" }, mandatory = true, help = "SCE property name") final File localFile,
			@CliOption(key = { "reportFile" }, mandatory = true, help = "Result report target path") final File targetFile,
			@CliOption(key = { "dryRun" }, mandatory = false, help = "Dry run flag, enabled by default", unspecifiedDefaultValue = "true") final boolean dryRun,
			@CliOption(key = { "xmlReportFile" }, mandatory = false, help = "Path to save XML formatted report") final File xmlReportFile,
			@CliOption(key = { "emailReportFile" }, mandatory = false, help = "Email report target path") final File emailReportFile)
			throws IOException, JAXBException {

		boolean success = false;
		SCECliConfigurationProperties localProps;
		try {
			JAXBContext jaxbContext = JAXBContext.newInstance(SCECliConfigurationProperties.class);
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			localProps = (SCECliConfigurationProperties) jaxbUnmarshaller.unmarshal(localFile);
		} catch (JAXBException e) {
			e.printStackTrace();
			return "Failed to parse XML file";
		}

		Map<String, ConfigurationItem> serverProps;
		try {
			serverProps = sceClient.getAll();
		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to load current configuration from server";
		}

		List<DiffReportItem> diffItems = new ArrayList<DiffReportItem>();

		for (SCECliConfigurationPropertyBean prop : localProps.getConfigurationPrperties()) {

			ConfigurationItem serverItem = serverProps.get(prop.getName());

			DiffReportItem item;
			if (prop.isDelete()) {
				item = buildDiffItemForDelete(serverItem, prop);
			} else if (serverItem == null) {
				item = buildDiffItemForCreate(prop);
			} else {
				item = buildDiffItemForOthers(serverItem, prop);
			}

			if (prop.getValue().startsWith("__IGNORE__")) {
				item = updateForIgnore(item, prop.getValue().replace("__IGNORE__", ""));
			}
			if (isIgnoredProperty(prop.getName())) {
				item = updateForIgnore(item, "IGNORED_BY_RULE");
			}

			diffItems.add(item);
		}

		for (DiffReportItem item : diffItems) {
			if (item.getOperation() == Operation.CREATE || item.getOperation() == Operation.UPDATE) {
				if (dryRun) {
					item.setSuccess("Skipped (dry run)");
					continue;
				}
				try {
					sceClient.createOrUpdate(item.getKey(), item.getNewValue());
					item.setSuccess("Success");
				} catch (Exception e) {
					e.printStackTrace();
					item.setComment("Failed... " + e.getMessage());
				}
			}
			if (item.getOperation() == Operation.DELETE) {
				if (dryRun) {
					item.setSuccess("Skipped (dry run)");
					continue;
				}
				try {
					sceClient.delete(item.getKey());
					item.setSuccess("Success");
				} catch (Exception e) {
					e.printStackTrace();
					item.setComment("Failed... " + e.getMessage());
				}
			}

			boolean sensitiveProp = isSensitiveProperty(item.getKey());
			logger.debug("{} - is sensitive property: {}", item.getKey(), sensitiveProp);
			item.setSensitive(sensitiveProp);

		}

		DiffReport report = new DiffReport();
		Collections.sort(diffItems);
		report.setItems(diffItems);

		if (xmlReportFile != null) {
			xmlReportFile.getParentFile().mkdirs();
			Files.write(XmlUtils.marshal(DiffReport.class, report, true), xmlReportFile, Charsets.UTF_8);
			logger.info("XML report saved to {}", xmlReportFile.getAbsolutePath());
		}

		try {
			TransformerFactory tf = TransformerFactory.newInstance();

			StreamSource xslt = new StreamSource(getClass().getResourceAsStream("/sce-diff-report-stylesheet.xsl"));

			Transformer transformer = tf.newTransformer(xslt);

			// Source
			JAXBContext jc = JAXBContext.newInstance(DiffReport.class);
			JAXBSource source = new JAXBSource(jc, report);

			// Result
			StreamResult result = new StreamResult(targetFile);

			// Transform
			transformer.setParameter("fullOutput", "true");
			transformer.setParameter("env", getConfigProperties().getProperty("env.displayName", "UNKNOWN"));
			transformer.setParameter("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date()));
			transformer.setParameter("css",
					IOUtils.toString(getClass().getResourceAsStream("/report-styles.css"), "UTF-8"));

			transformer.transform(source, result);

			if (emailReportFile != null) {
				// Result
				result = new StreamResult(emailReportFile);

				// Transform
				transformer.setParameter("fullOutput", "false");
				transformer.setParameter("env", getConfigProperties().getProperty("env.displayName", "UNKNOWN"));
				transformer.setParameter("timestamp", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z").format(new Date()));
				transformer.setParameter("css",
						IOUtils.toString(getClass().getResourceAsStream("/report-styles.css"), "UTF-8"));

				transformer.transform(source, result);
			}

		} catch (Exception e) {
			e.printStackTrace();
			return "Unable to generate dry run report";
		}

		success = true;

		if (success) {

			return "Done. " + (dryRun ? "Dry run" : "Operation") + " report is saved at "
					+ targetFile.getCanonicalPath();
		} else {
			return "Failed";
		}

	}

	@CliCommand(value = "logout", help = "Log off from current SCE session")
	public String logout() {
		sceClient.logout();
		return "Logged out";
	}

	private DiffReportItem buildDiffItemForDelete(ConfigurationItem currentItem, SCECliConfigurationPropertyBean newItem) {
		DiffReportItem diffItem = new DiffReportItem();

		if (currentItem == null) {
			diffItem.setKey(newItem.getName());
			diffItem.setComment("Attempting to delete non existing property, this will be ignored");
			diffItem.setOperation(Operation.NONE);
		} else {
			diffItem.setCurrentValue(currentItem.getValue());
			diffItem.setNewValue("");
			diffItem.setId(currentItem.getId());
			diffItem.setKey(currentItem.getKey());

			if (currentItem.isReadOnly()) {
				diffItem.setComment("Attempting to delete read only property, this will be ignored");
				diffItem.setOperation(Operation.NONE);
				diffItem.setNewValue(currentItem.getValue());
			} else {
				diffItem.setOperation(Operation.DELETE);
			}

			diffItem.setDiff(diffLib.diff_main(diffItem.getCurrentValue(), diffItem.getNewValue()));
			diffLib.diff_cleanupSemantic(diffItem.getDiff());
			diffItem.setPrettyHtmlDiff(diffLib.diff_prettyHtml(diffItem.getDiff()));

			diffItem.setIdentical(isIdentical(diffItem));
		}

		return diffItem;
	}

	private DiffReportItem buildDiffItemForCreate(SCECliConfigurationPropertyBean newItem) {

		DiffReportItem diffItem = new DiffReportItem();

		diffItem.setOperation(Operation.CREATE);

		diffItem.setKey(newItem.getName());
		diffItem.setCurrentValue("");
		diffItem.setNewValue(newItem.getValue());

		diffItem.setDiff(diffLib.diff_main(diffItem.getCurrentValue(), diffItem.getNewValue()));
		diffLib.diff_cleanupSemantic(diffItem.getDiff());
		diffItem.setPrettyHtmlDiff(diffLib.diff_prettyHtml(diffItem.getDiff()));

		diffItem.setIdentical(isIdentical(diffItem));

		return diffItem;
	}

	private DiffReportItem buildDiffItemForOthers(ConfigurationItem currentItem, SCECliConfigurationPropertyBean newItem) {

		DiffReportItem diffItem = new DiffReportItem();

		diffItem.setId(currentItem.getId());

		diffItem.setKey(currentItem.getKey());
		diffItem.setCurrentValue(currentItem.getValue());
		diffItem.setNewValue(newItem.getValue());

		diffItem.setDiff(diffLib.diff_main(diffItem.getCurrentValue(), diffItem.getNewValue()));
		diffLib.diff_cleanupSemantic(diffItem.getDiff());
		diffItem.setIdentical(isIdentical(diffItem));
		diffItem.setPrettyHtmlDiff(diffLib.diff_prettyHtml(diffItem.getDiff()));

		if (diffItem.isIdentical()) {
			diffItem.setOperation(Operation.NONE);
		} else {
			if (currentItem.isReadOnly()) {
				diffItem.setOperation(Operation.NONE);
				diffItem.setComment("Attempting to update read only property, this will be skipped");
				diffItem.setNewValue(currentItem.getValue());
			} else {
				diffItem.setOperation(Operation.UPDATE);
			}
		}

		return diffItem;
	}

	private DiffReportItem updateForIgnore(DiffReportItem diffItem, String reason) {

		diffItem.setComment("Intentionally ignoring this value. Reason: " + reason);

		if (diffItem.getNewValue().startsWith("__IGNORE__")) {
			diffItem.setNewValue(diffItem.getCurrentValue());
		}

		diffItem.setOperation(Operation.NONE);

		diffItem.setDiff(diffLib.diff_main(diffItem.getCurrentValue(), diffItem.getNewValue()));
		diffLib.diff_cleanupSemantic(diffItem.getDiff());
		diffItem.setIdentical(isIdentical(diffItem));
		diffItem.setPrettyHtmlDiff(diffLib.diff_prettyHtml(diffItem.getDiff()));

		return diffItem;
	}

	private boolean isIdentical(DiffReportItem item) {
		LinkedList<Diff> diffs = item.getDiff();
		if (diffs == null || diffs.size() == 0
				|| (diffs.size() == 1 && diffs.get(0).operation == org.webjars.diff_match_patch.Operation.EQUAL)) {
			return true;
		}
		return false;
	}

	private boolean isIgnoredProperty(String name) {
		if (name == null) {
			return false;
		}

		Properties props;
		try {
			props = builder.getProperties();
			Iterable<String> list = Splitter.on(",").trimResults().split(props.getProperty("ignoredProperties", ""));
			for (String regex : list) {
				if (name.matches(regex)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("exception caught", e);
		}
		return false;
	}

	private boolean isSensitiveProperty(String name) {
		if (name == null) {
			return false;
		}

		Properties props;
		try {
			props = builder.getProperties();
			Iterable<String> excludes = Splitter.on(",").trimResults()
					.split(props.getProperty("sensitivePropertiesExcludes", ""));
			for (String ex : excludes) {
				if (name.equals(ex)) {
					return false;
				}
			}
			Iterable<String> list = Splitter.on(",").trimResults()
					.split(props.getProperty("sensitivePropertiesRegexp", ""));
			for (String regex : list) {
				if (name.matches(regex)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("exception caught", e);
		}
		return false;
	}

	private boolean isXmlProperty(String name) {
		if (name == null) {
			return false;
		}

		Properties props;
		try {
			props = getGlobalProperties();
			Iterable<String> excludes = Splitter.on(",").trimResults()
					.split(props.getProperty("xmlPropertiesExcludes", ""));
			for (String ex : excludes) {
				if (name.equals(ex)) {
					return false;
				}
			}
			Iterable<String> list = Splitter.on(",").trimResults().split(props.getProperty("xmlProperties", ""));
			if (name.endsWith(".xml")) {
				return true;
			}
			for (String key : list) {
				if (name.equals(key)) {
					return true;
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("exception caught", e);
		}
		return false;
	}

	public Properties getConfigProperties() {
		Properties global = new Properties();
		Properties env = new Properties();
		try {
			global.load(new FileInputStream(new File(System.getProperty("globalConfig"))));
			env.load(new FileInputStream(new File(System.getProperty("config"))));
		} catch (IOException e) {
			logger.error("Unable to load configuraiton properties", e);
			throw new IllegalStateException("Unable to load configuraiton properties", e);
		}

		Properties merged = new Properties();
		merged.putAll(global);
		merged.putAll(env);
		return merged;
	}

	private Properties getGlobalProperties() throws FileNotFoundException, IOException {
		Properties props = new Properties();
		props.load(new FileInputStream(new File(System.getProperty("globalConfig"))));
		return props;
	}

}
