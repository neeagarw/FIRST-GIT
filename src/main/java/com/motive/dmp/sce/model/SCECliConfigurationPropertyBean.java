package com.motive.dmp.sce.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import com.motive.dcm.nbi.beans.ConfigurationPropertyBean;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "configurationProperty")
public class SCECliConfigurationPropertyBean extends ConfigurationPropertyBean {

	@XmlAttribute
	private boolean delete;

	public boolean isDelete() {
		return delete;
	}

}