package org.jnetwork.database;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jnetwork.Connection;
import org.jnetwork.DataPackage;
import org.jnetwork.Protocol;
import org.jnetwork.TCPConnection;

public class QueryConnection {
	private Connection client;

	public QueryConnection(String host, int port, Protocol protocol) throws UnknownHostException, IOException {
		client = protocol.getNetworkFactory().createConnection(host, port);
	}

	public synchronized EntrySet query(String query) throws IOException, QueryException {
		try {
			client.writeUnshared(new DataPackage(Query.parseQuery(query)).setMessage("SERVER_DATABASE_QUERY"));
			DataPackage response = (DataPackage) client.readUnshared();
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
		client.writeUnshared(new DataPackage().setMessage("CLIENT_DATABASE_CLOSE_CONNECTION"));
		if (client instanceof TCPConnection) {
			((TCPConnection) client).getOutputStream().flush();
		}
		client.close();
	}
}
