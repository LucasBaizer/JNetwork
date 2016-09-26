package org.jnetwork.ui.database;

import java.io.File;
import java.io.IOException;

import org.jnetwork.database.Entry;
import org.jnetwork.database.EntrySet;
import org.jnetwork.database.Table;

public class DatabaseService {
	private static DatabaseService theStatus;
	private static final File cache = new File("db.cache");
	private static final CacheService<File> cacheService = new CacheService<>();

	private Table table;
	private EntrySet entries;

	public static void loadFromCache() {
		if (cache.exists()) {
			File cacheFile = cacheService.loadCache(cache, null);
			if (cacheFile.exists()) {
				try {
					setDatabase(Table.load(cacheFile));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private DatabaseService(Table table) {
		this.setTable(table);
	}

	public static DatabaseService getDatabase() {
		if (theStatus == null) {
			setDatabase(null);
		}
		return theStatus;
	}

	public static void setDatabase(Table table) {
		DatabaseService.theStatus = new DatabaseService(table);
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		if (table != null)
			cacheService.saveCache(cache, table.getTableFile());

		this.table = table;
	}

	public EntrySet getEntrySet() {
		return entries;
	}

	public void setEntries(EntrySet entries) {
		this.entries = entries;
	}

	public int indexOf(String entry) {
		int i = 0;
		for (Entry e : entries) {
			if (e.getEntryID().equals(entry)) {
				return i;
			}
			i++;
		}
		return -1;
	}
}
