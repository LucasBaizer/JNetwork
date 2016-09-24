package org.jnetwork.database;

import java.io.File;
import java.io.IOException;

public class Test {
	public static void main(String[] args) {
		try {
			Database database = new Database();
			database.addTable(Table.load(new File("tables/People.table")));

			Table table = database.getTable("People");
			// add(database, table, "Lucas Baizer", 13);
			// add(database, table, "Jacob Baizer", 19);
			for (Entry entry : database.query("SET ENTRY 551b7aec9ca6e3df TO [Lucas Baizer, *] IN People")) {
				System.out.println("Entry: " + entry.getEntryID());
				System.out.println("Time taken for query: " + entry.getQueryTime() + "ms");
				for (int i = 0; i < entry.getData().length; i++) {
					System.out.println(
							"\t" + table.getColumnHeaders()[i].getColumnName() + ": " + entry.getData()[i].toString());
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void add(Database database, Table table, String string, int a) throws IOException, QueryException {
		System.err.println();
		System.err.println();
		for (Entry entry : database.query("ADD [" + string + ", " + a + "] IN People")) {
			System.err.println("Entry: " + entry.getEntryID());
			for (int i = 0; i < entry.getData().length; i++) {
				System.err.println(
						"\t" + table.getColumnHeaders()[i].getColumnName() + ": " + entry.getData()[i].toString());
			}
		}
	}
}
