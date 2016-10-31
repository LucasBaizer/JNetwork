package org.jnetwork;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * A factory for creating TCP sockets.
 * 
 * @author Lucas Baizer
 */
public class TCPNetworkFactory extends NetworkFactory {
	TCPNetworkFactory() {
	}

	@Override
	public Connection createConnection(String host, int port) throws UnknownHostException, IOException {
		return new TCPConnection(host, port);
	}

	@Override
	public Server createServer(int port, ClientConnectionCallback l) throws ServerException {
		return new TCPServer(port, (TCPConnectionCallback) l);
	}
}
