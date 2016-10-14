package org.jnetwork.database;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Database implements Serializable {
	private static final long serialVersionUID = 5313886669423096055L;
	private Table[] tables = new Table[0];

	public Table[] getTables() {
		return tables;
	}

	public void setTables(Table[] tables) {
		this.tables = tables;
	}

	public void addTable(Table table) {
		List<Table> list = asList();
		list.add(table);
		tables = list.toArray(new Table[list.size()]);
	}

	public void removeTable(Table table) {
		List<Table> list = asList();
		list.remove(table);
		tables = list.toArray(new Table[list.size()]);
	}

	public Table getTable(String name) {
		for (Table t : tables) {
			if (t.getName().equals(name)) {
				return t;
			}
		}
		return null;
	}

	public EntrySet query(String request) throws IOException, QueryException {
		return query(Query.parseQuery(request));
	}

	public EntrySet query(Query query) throws IOException, QueryException {
		try {
			return getTable(query.getTableTarget()).query(query);
		} catch (NullPointerException e) {
			throw new QueryException("No table with name: " + query.getTableTarget());
		}
	}

	private ArrayList<Table> asList() {
		return (ArrayList<Table>) new ArrayList<>(Arrays.asList(tables));
	}
}
