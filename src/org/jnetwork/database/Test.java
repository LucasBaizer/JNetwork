package org.jnetwork.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import org.jnetwork.Server;
import org.jnetwork.TCPServer;

public class Test {
	public static void main(String[] args) {
		try {
			Database database = new Database();

			try {
				database.addTable(Table.load(new File("tables/People.table")));
			} catch (FileNotFoundException e) {
				database.addTable(new Table("tables", "People",
						new ColumnHeader[] { new ColumnHeader("Name", ColumnHeader.STORAGE_TYPE_STRING),
								new ColumnHeader("Age", ColumnHeader.STORAGE_TYPE_INTEGER) }));
			}

			Server server = new TCPServer(1337, new DatabaseServerConnectionHandler(database));
			server.start();

			QueryConnection conn = QueryConnection.createConnection("localhost", 1337);
			for (Entry entry : conn.query("SET ENTRY d3e8bae533d94dee TO [*, 19] IN People")) {
				System.out.println("Entry: " + entry.getEntryID());
				System.out.println("Time taken for query: " + entry.getQueryTime() + "ms");
				for (int i = 0; i < entry.getData().length; i++) {
					System.out.println("\t" + database.getTable("People").getColumnHeaders()[i].getColumnName() + ": "
							+ entry.getData()[i].toString());
				}
			}
			conn.closeConnection();

			server.close();
		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
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
