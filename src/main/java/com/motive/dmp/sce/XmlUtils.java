package com.motive.dmp.sce;

import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.parsers.DocumentBuilderFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Node;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.InputSource;

public class XmlUtils {
	
	private static final Logger logger = LoggerFactory.getLogger(XmlUtils.class);

	public static String stripCDATA(String s) {
		s = s.trim();
		String startString = "<![CDATA[";
		if (s.startsWith(startString)) {
			s = s.substring(startString.length());
			int i = s.indexOf("]]>");
			if (i == -1) {
				throw new IllegalStateException(
						"argument starts with <![CDATA[ but cannot find pairing ]]&gt;");
			}
			s = s.substring(0, i);
		}
		return s;
	}

	public static String prettyPrintXml(String xml) {
		try {
			final InputSource src = new InputSource(new StringReader(xml));
			final Node document = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().parse(src).getDocumentElement();
			final Boolean keepDeclaration = Boolean.valueOf(xml
					.startsWith("<?xml"));

			final DOMImplementationRegistry registry = DOMImplementationRegistry
					.newInstance();
			final DOMImplementationLS impl = (DOMImplementationLS) registry
					.getDOMImplementation("LS");
			final LSSerializer writer = impl.createLSSerializer();

			writer.getDomConfig().setParameter("format-pretty-print",
					Boolean.TRUE); // Set this to true if the output needs to be
									// beautified.
			writer.getDomConfig().setParameter("xml-declaration",
					keepDeclaration); // Set this to true if the declaration is
										// needed to be outputted.

			return writer.writeToString(document);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
	
	@SuppressWarnings("unchecked")
	public static <T> T unmarshal(String xml, Class<T> clazz) {

		logger.debug("Ceating a new instance of {} from XML string [{}]", clazz.getName(), xml);

		if (xml == null) {
			return null;
		}

		T instance = null;

		try {
			xml = stripCDATA(xml);
			instance = (T) JAXBContext.newInstance(clazz).createUnmarshaller().unmarshal(new StringReader(xml));
		} catch (JAXBException e) {
			logger.error("Failed to convert XML to ExecutionFilter object", e);
		}
		logger.debug("Returning an instance: [{}]", instance);
		return instance;
	}

	public static String marshal(Class<?> clazz, Object obj) throws JAXBException {
		return marshal(clazz, obj, true);
	}

	public static String marshal(Class<?> clazz, Object obj, boolean writeDeclaration) throws JAXBException {

		logger.debug("Converting an instance of [{}] to XML", clazz.getName());
		JAXBContext jaxbContext = JAXBContext.newInstance(clazz);
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

		if (!writeDeclaration) {
			jaxbMarshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
		}

		StringWriter writer = new StringWriter();
		jaxbMarshaller.marshal(obj, writer);
		return writer.toString();
	}
}