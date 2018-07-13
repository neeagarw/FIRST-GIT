package com.motive.dmp.sce.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "configurationProperties")
public class SCECliConfigurationProperties {

	@XmlElement(name = "configurationProperty")
	private SCECliConfigurationPropertyBean[] configurationProperties;

	public SCECliConfigurationPropertyBean[] getConfigurationPrperties() {
		return configurationProperties;
	}

}