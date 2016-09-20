package org.jnetwork.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

public class EntrySet implements Serializable {
	private static final long serialVersionUID = 6891274761747429375L;

	private ArrayList<Entry> entries = new ArrayList<>();

	public void setEntries(Entry[] entries) {
		this.entries = new ArrayList<>(Arrays.asList(entries));
	}

	public Entry[] getEntries() {
		return this.entries.toArray(new Entry[this.entries.size()]);
	}

	public void addEntry(Entry e) {
		this.entries.add(e);
	}

	public void removeEntry(Entry e) {
		this.entries.remove(e);
	}
}
