package org.jnetwork;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jnetwork.listener.ClientConnectionListener;
import org.jnetwork.listener.TCPConnectionListener;

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
	public Server createServer(int port, ClientConnectionListener l) throws ServerException {
		return new TCPServer(port, (TCPConnectionListener) l);
	}
}
