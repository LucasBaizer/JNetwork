package test;

import java.io.File;
import java.io.IOException;

import org.jnetwork.database.Entry;
import org.jnetwork.database.EntrySet;
import org.jnetwork.database.Table;

public class DatabaseService {
	private static DatabaseService theStatus;
	private static final File cache = new File("db.cache");
	private static final CacheService<Table> cacheService = new CacheService<>();

	private Table table;
	private EntrySet entries;

	public static void loadFromCache() {
		if (cache.exists()) {
			setDatabase(cacheService.loadCache(cache, null));
		}
	}

	private DatabaseService(Table table) {
		this.setTable(table);
	}

	public static DatabaseService getDatabase() {
		return theStatus;
	}

	public static void setDatabase(Table table) {
		try {
			DatabaseService.theStatus = new DatabaseService(Table.load(table.getTableFile()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public Table getTable() {
		return table;
	}

	public void setTable(Table table) {
		cacheService.saveCache(cache, table);

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
