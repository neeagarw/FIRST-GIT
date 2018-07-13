package com.motive.dmp.sce;

import java.util.LinkedList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.webjars.diff_match_patch.Diff;

import com.google.common.base.Strings;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "property-item")
public class DiffReportItem implements Comparable<DiffReportItem> {

	@XmlElement(name = "operation")
	private Operation operation;

	@XmlElement(name = "name")
	private String key;

	@XmlElement(name = "curr")
	private String currentValue;

	@XmlElement(name = "new")
	private String newValue;

	@XmlTransient
	private LinkedList<Diff> diff;

	@XmlElement(name = "html")
	private String prettyHtmlDiff;

	@XmlElement(name = "comment")
	private String comment;

	@XmlElement(name = "identical")
	private boolean identical;

	@XmlElement(name = "id")
	private long id;

	@XmlElement(name = "success")
	private String success;

	@XmlElement(name = "sensitive")
	private boolean sensitive;

	public boolean isSensitive() {
		return sensitive;
	}

	public void setSensitive(boolean sensitive) {
		this.sensitive = sensitive;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public Operation getOperation() {
		return operation;
	}

	public void setOperation(Operation operation) {
		this.operation = operation;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getCurrentValue() {
		return currentValue;
	}

	public void setCurrentValue(String currentValue) {
		this.currentValue = currentValue;
	}

	public String getNewValue() {
		return newValue;
	}

	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}

	public LinkedList<Diff> getDiff() {
		return diff;
	}

	public void setDiff(LinkedList<Diff> diff) {
		this.diff = diff;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

	public boolean isIdentical() {
		return identical;
	}

	public void setIdentical(boolean identical) {
		this.identical = identical;
	}

	public String getSuccess() {
		return success;
	}

	public void setSuccess(String success) {
		this.success = success;
	}

	@Override
	public String toString() {
		return "DiffReportItem [operation=" + operation + ", key=" + key
				+ ", currentValue=" + currentValue + ", newValue=" + newValue
				+ ", diff=" + diff + ", prettyHtmlDiff=" + prettyHtmlDiff
				+ ", comment=" + comment + ", identical=" + identical + ", id="
				+ id + ", success=" + success + "]";
	}

	public String getPrettyHtmlDiff() {
		return prettyHtmlDiff;
	}

	public void setPrettyHtmlDiff(String prettyHtmlDiff) {
		this.prettyHtmlDiff = prettyHtmlDiff;
	}

	public static enum Operation {
		CREATE, UPDATE, DELETE, NONE
	}

	@Override
	public int compareTo(DiffReportItem o) {
		return Strings.nullToEmpty(this.getKey()).compareTo(o.getKey());
	}

}