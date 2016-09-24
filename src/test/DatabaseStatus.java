package test;

import java.io.File;

import org.jnetwork.database.EntrySet;
import org.jnetwork.database.Table;

public class DatabaseStatus {
	private static DatabaseStatus theStatus;
	private static final File cache = new File("db.cache");
	private static final CacheService<Table> cacheService = new CacheService<>();

	private Table table;
	private EntrySet entries;

	public static void loadFromCache() {
		if (cache.exists()) {
			setDatabase(cacheService.loadCache(cache, null));
		}
	}

	private DatabaseStatus(Table table) {
		this.setTable(table);
	}

	public static DatabaseStatus getDatabase() {
		return theStatus;
	}

	public static void setDatabase(Table table) {
		DatabaseStatus.theStatus = new DatabaseStatus(table);
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
}
