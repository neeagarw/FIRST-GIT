package com.motive.dmp.sce;

public class ConfigurationItem implements Comparable<ConfigurationItem> {

	long id;
	String key;
	String value;
	boolean encrypted;
	boolean readOnly;

	public long getId() {
		return id;
	}

	public String getKey() {
		if (key != null) {
			return key.trim();
		}
		return key;
	}

	public String getValue() {
		if (value != null) {
			return value.trim();
		}
		return value;
	}

	public boolean isEncrypted() {
		return encrypted;
	}

	public boolean isReadOnly() {
		return readOnly;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (encrypted ? 1231 : 1237);
		result = prime * result + (int) (id ^ (id >>> 32));
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + (readOnly ? 1231 : 1237);
		result = prime * result + ((value == null) ? 0 : value.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ConfigurationItem other = (ConfigurationItem) obj;
		if (encrypted != other.encrypted)
			return false;
		if (id != other.id)
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (readOnly != other.readOnly)
			return false;
		if (value == null) {
			if (other.value != null)
				return false;
		} else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ConfigurationItem [id=" + id + ", key=" + key + ", value="
				+ value + ", encrypted=" + encrypted + ", readOnly=" + readOnly
				+ "]";
	}

	@Override
	public int compareTo(ConfigurationItem anotherItem) {
		return Long.valueOf(id).compareTo(anotherItem.id);
	}

}