package org.jnetwork;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * A factory used for creating connections and servers, typically from a
 * {@link Protocol}.
 * 
 * @author Lucas Baizer
 */
public abstract class NetworkFactory {
	/**
	 * Creates a new connection denoted by this factory.
	 * 
	 * @param host
	 *            - The host of the connection. See
	 *            {@link Connection#Connection(String, int)}.
	 * @param port
	 *            - The port of the connection. See
	 *            {@link Connection#Connection(String, int)}.
	 * @return The connection.
	 * @throws UnknownHostException
	 *             If the host does not exist.
	 * @throws IOException
	 *             If an error occurs while creating the connection.
	 */
	public abstract Connection createConnection(String host, int port) throws UnknownHostException, IOException;

	/**
	 * Creates a new server denoted by this factory.s
	 * 
	 * @param port
	 *            - The port to bind the server to. See
	 *            {@link Server#Server(int, ClientConnectionListener)}.
	 * @param l
	 *            - The event to be called when a client connects. See
	 *            {@link Server#Server(int, ClientConnectionListener)}.
	 * @return The server.
	 * @throws ServerException
	 *             If an error occurs while creating the server.
	 */
	public abstract Server createServer(int port, ClientConnectionListener l) throws ServerException;
}
