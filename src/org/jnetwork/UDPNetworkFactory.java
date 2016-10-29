package org.jnetwork;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * A factory for creating UDP sockets.
 * 
 * @author Lucas Baizer
 */
public class UDPNetworkFactory extends NetworkFactory {
	UDPNetworkFactory() {
	}

	@Override
	public Connection createConnection(String host, int port) throws UnknownHostException, IOException {
		return new UDPConnection(host, port);
	}

	@Override
	public Server createServer(int port, ClientConnectionListener l) throws ServerException {
		return new UDPServer(port, (UDPConnectionListener) l);
	}
}
