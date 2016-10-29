package org.jnetwork.ui.database;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jnetwork.Protocol;
import org.jnetwork.Server;
import org.jnetwork.TCPServer;
import org.jnetwork.database.Database;
import org.jnetwork.database.DatabaseServerConnectionHandler;
import org.jnetwork.database.QueryConnection;

public class DatabaseService {
	private static Server localServer;
	private static DatabaseServerConnectionHandler serverHandler;

	private static QueryConnection connection;
	private static String currentTableName;

	public static void connect(String host, int port) throws UnknownHostException, IOException {
		if (connection != null) {
			connection.closeConnection();
		}
		connection = new QueryConnection(host, port, Protocol.TCP);
	}

	public static QueryConnection getConnection() {
		return connection;
	}

	public static boolean isConnected() {
		return connection != null;
	}

	public static String getCurrentTableName() {
		return currentTableName;
	}

	public static void setCurrentTableName(String currentTableName) {
		DatabaseService.currentTableName = currentTableName;
	}

	public static DatabaseServerConnectionHandler getLocalServerHandler() {
		return serverHandler;
	}

	public static boolean isLocalServer() {
		return localServer != null;
	}

	public static void startLocalServer() throws IOException {
		if (localServer == null) {
			localServer = new TCPServer(1337, serverHandler = new DatabaseServerConnectionHandler(new Database()));
			localServer.start();
		}
	}
}