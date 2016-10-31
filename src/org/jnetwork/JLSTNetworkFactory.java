package org.jnetwork;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * A factory for creating JLST sockets.
 * 
 * @author Lucas Baizer
 */
public class JLSTNetworkFactory extends NetworkFactory {
	JLSTNetworkFactory() {
	}

	@Override
	public Connection createConnection(String host, int port) throws UnknownHostException, IOException {
		try {
			return new JLSTConnection(host, port);
		} catch (CryptographyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Server createServer(int port, ClientConnectionCallback l) throws ServerException {
		try {
			return new JLSTServer(port, (TCPConnectionCallback) l, null);
		} catch (CryptographyException e) {
			throw new ServerException(e);
		}
	}
}
