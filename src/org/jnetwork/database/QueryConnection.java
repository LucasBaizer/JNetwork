package org.jnetwork.database;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jnetwork.Connection;
import org.jnetwork.DataPackage;

public class QueryConnection {
	private Connection client;

	private QueryConnection(String host, int port) throws UnknownHostException, IOException {
		client = new Connection(host, port);
	}

	public static QueryConnection createConnection(String host, int port) throws UnknownHostException, IOException {
		return new QueryConnection(host, port);
	}

	public synchronized EntrySet query(String query) throws IOException, QueryException {
		try {
			client.getOutputStream()
					.writeUnshared(new DataPackage(Query.parseQuery(query)).setMessage("SERVER_DATABASE_QUERY"));
			DataPackage response = (DataPackage) client.getInputStream().readUnshared();
			if (response.getMessage().equals("SERVER_DATABASE_QUERY_RESPONSE_ERROR")) {
				throw (QueryException) response.getObjects()[0];
			} else {
				return (EntrySet) response.getObjects()[0];
			}
		} catch (ClassNotFoundException e) {
			throw new IOException(e.getClass().getName() + ": " + e.getMessage());
		}
	}

	public synchronized void closeConnection() throws IOException {
		client.getOutputStream().writeObject(new DataPackage().setMessage("CLIENT_DATABASE_CLOSE_CONNECTION"));
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					client.close();
				} catch (InterruptedException e) {
					return;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}, "Close-Database-Socket").start();
	}
}
