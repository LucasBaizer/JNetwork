package example;

import java.io.File;

import org.jnetwork.Protocol;
import org.jnetwork.Server;
import org.jnetwork.TCPServer;
import org.jnetwork.database.ColumnHeader;
import org.jnetwork.database.Database;
import org.jnetwork.database.DatabaseServerConnectionHandler;
import org.jnetwork.database.Entry;
import org.jnetwork.database.QueryConnection;
import org.jnetwork.database.Table;

public class DatabaseEntryTransfer {
	public static void main(String[] args) {
		try {
			// creates new folder called "tables" in current working directory
			File tablesFolder = new File("tables");
			tablesFolder.mkdir();

			Database database = new Database();
			// creates Employees.table file in tables directory
			database.addTable(new Table("tables", "Employees",
					new ColumnHeader[] { new ColumnHeader("Name", ColumnHeader.STORAGE_TYPE_STRING),
							new ColumnHeader("Age", ColumnHeader.STORAGE_TYPE_INTEGER) }));

			Server databaseServer = new TCPServer(1337, new DatabaseServerConnectionHandler(database));
			databaseServer.start();

			QueryConnection query = new QueryConnection("localhost", 1337, Protocol.TCP);
			// add an employee named Foo Bar of age 46
			query.query("ADD [Foo Bar, 46] IN Employees");
			// add an employee named Joe Blow of age 96
			query.query("ADD [Joe Blow, 96] IN Employees");

			// for-each through employees of age 96
			for (Entry employee : query.query("GET WHERE Age INCLUDES 6 IN Employees")) {
				// prints Joe Blow
				System.out.println(employee.getData("Name"));
			}

			// close connection and server
			query.closeConnection();
			databaseServer.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
