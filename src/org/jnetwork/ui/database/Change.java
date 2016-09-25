package org.jnetwork.ui.database;

import java.io.Serializable;
import java.util.ArrayList;

public class Change {
	public static final int REMOVE = 0;
	public static final int SET = 1;
	public static final int ADD = 2;

	private int change;
	private String entryID;
	private String changeID;
	private ArrayList<Serializable> originalData;
	private ArrayList<Serializable> data;

	public Change(String entryID, int change, ArrayList<Serializable> originalData, ArrayList<Serializable> data) {
		this.entryID = entryID;
		this.originalData = originalData;
		this.data = data;
		this.change = change;
		this.setChangeID(entryID);

		if (data == null) {
			this.data = new ArrayList<>();
			this.originalData = new ArrayList<>();
		}
	}

	public String getEntryID() {
		return entryID;
	}

	public void setEntryID(String entryID) {
		this.entryID = entryID;
	}

	public ArrayList<Serializable> getData() {
		return data;
	}

	public void setData(ArrayList<Serializable> data) {
		this.data = data;
	}

	public int getChange() {
		return change;
	}

	public void setChange(int change) {
		this.change = change;
	}

	public ArrayList<Serializable> getOriginalData() {
		return originalData;
	}

	public void setOriginalData(ArrayList<Serializable> originalData) {
		this.originalData = originalData;
	}

	public String getChangeString() {
		String str = "";
		if (data != null) {
			for (Serializable obj : data) {
				str += obj.toString() + ", ";
			}
		}
		return str.substring(0, str.length() - 2);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Change) {
			return ((Change) o).changeID.equals(this.changeID);
		}
		return false;
	}

	public String getChangeID() {
		return changeID;
	}

	public Change setChangeID(String changeID) {
		this.changeID = changeID;
		return this;
	}
}
