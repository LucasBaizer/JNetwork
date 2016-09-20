package org.jnetwork.database;

import java.math.BigInteger;
import java.security.SecureRandom;

public class Test {
	public static void main(String[] args) {
		try {
			Database database = new Database();

			database.addTable(
					new Table("People", new ColumnHeader[] { new ColumnHeader("Name", ColumnHeader.STORAGE_TYPE_STRING),
							new ColumnHeader("Age", ColumnHeader.STORAGE_TYPE_INTEGER) }));

			SecureRandom random = new SecureRandom();
			for (int i = 0; i < 100; i++) {
				database.query(
						"add [" + new BigInteger(24, random).toString(36) + ", " + random.nextInt(100) + "] in People");
			}

			for (Entry e : database.query("GET IN People").getEntries()) {
				System.out.println(e.getData("Name"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
