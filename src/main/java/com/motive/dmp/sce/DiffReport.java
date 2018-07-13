package com.motive.dmp.sce;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "dry-run-report")
public class DiffReport {

	@XmlElement(name = "property-item")
	private List<DiffReportItem> items;

	public List<DiffReportItem> getItems() {
		return items;
	}

	public void setItems(List<DiffReportItem> items) {
		this.items = items;
	}

	@Override
	public String toString() {
		return "DiffReport [items=" + items + "]";
	}

}