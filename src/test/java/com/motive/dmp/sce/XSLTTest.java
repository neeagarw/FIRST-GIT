package com.motive.dmp.sce;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.util.JAXBSource;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:/applicationContext-test.xml" })
public class XSLTTest {

	Logger logger = LoggerFactory.getLogger(XSLTTest.class);

	@Test
	public void xsltTest() throws Exception {

		File xmlReportFile = new File("src/test/resources/report.xml");
		TransformerFactory tf = TransformerFactory.newInstance();

		StreamSource xslt = new StreamSource(new FileInputStream(new File(
				"src/main/resources/sce-diff-report-stylesheet.xsl")));

		Transformer transformer = tf.newTransformer(xslt);

		// Source
		DiffReport xmlReport = XmlUtils.unmarshal(Files.toString(xmlReportFile, Charsets.UTF_8), DiffReport.class);
		JAXBContext jc = JAXBContext.newInstance(DiffReport.class);
		JAXBSource source = new JAXBSource(jc, xmlReport);

		File reportFile = new File("target/fullReport.html");

		// Result
		StreamResult result = new StreamResult(reportFile);

		// Transform
		transformer.setParameter("env", "DEV2");
		transformer.setParameter("fullOutput", "true");
		transformer.setParameter("timestamp", new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss z").format(new Date()));
		transformer
				.setParameter("css", IOUtils.toString(getClass().getResourceAsStream("/report-styles.css"), "UTF-8"));

		transformer.transform(source, result);

		// Now generate summary
		// Source
		reportFile = new File("target/summaryReport.html");

		// Result
		result = new StreamResult(reportFile);

		// Transform
		transformer.setParameter("env", "DEV2");
		transformer.setParameter("fullOutput", "false");
		transformer.setParameter("timestamp", new SimpleDateFormat("dd MMMM yyyy, HH:mm:ss z").format(new Date()));
		transformer
				.setParameter("css", IOUtils.toString(getClass().getResourceAsStream("/report-styles.css"), "UTF-8"));

		transformer.transform(source, result);

	}

}
