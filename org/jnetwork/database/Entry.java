package org.jnetwork.database;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public class Entry implements Serializable {
	private static final long serialVersionUID = -5657184436410947263L;

	private LinkedHashMap<String, Serializable> data = new LinkedHashMap<>();
	private String entryID;
	private long queryTime;

	public Entry(String entryID) {
		this.entryID = entryID;
	}

	public Entry(Map<String, Serializable> data, String entryID) {
		this.data = new LinkedHashMap<>(data);
		this.entryID = entryID;
	}

	public Serializable[] getData() {
		return data.values().toArray(new Serializable[data.size()]);
	}

	public Serializable getData(String column) {
		return data.get(column);
	}

	public void setData(String column, Serializable value) {
		data.put(column, value);
	}

	public void removeData(String column) {
		data.remove(column);
	}

	public String getEntryID() {
		return entryID;
	}

	public void setEntryID(String entryID) {
		this.entryID = entryID;
	}

	public long getQueryTime() {
		return queryTime;
	}

	void setQueryTime(long queryTime) {
		this.queryTime = queryTime;
	}
}
