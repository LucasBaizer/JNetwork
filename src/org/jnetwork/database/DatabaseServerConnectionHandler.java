package org.jnetwork.database;

import org.jnetwork.DataPackage;
import org.jnetwork.SocketPackage;
import org.jnetwork.TCPConnectionCallback;

public class DatabaseServerConnectionHandler implements TCPConnectionCallback {
	private Database db;

	public DatabaseServerConnectionHandler(Database database) {
		this.db = database;
	}

	@Override
	public void clientConnected(SocketPackage event) {
		try {
			while (true) {
				DataPackage in = (DataPackage) event.getConnection().readObject();
				if (in.getMessage().equals("SERVER_DATABASE_QUERY")) {
					Query query = (Query) in.getObjects()[0];
					try {
						EntrySet set = db.query(query);
						event.getConnection()
								.writeObject(new DataPackage(set).setMessage("SERVER_DATABASE_QUERY_RESPONSE_SUCCESS"));
					} catch (QueryException e) {
						event.getConnection()
								.writeObject(new DataPackage(e).setMessage("SERVER_DATABASE_QUERY_RESPONSE_ERROR"));
					}
				} else {
					return;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public Database getDatabase() {
		return db;
	}

	public void setDatabase(Database db) {
		this.db = db;
	}
}
