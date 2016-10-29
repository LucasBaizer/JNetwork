package org.jnetwork;

import java.io.IOException;
import java.net.UnknownHostException;

/**
 * A factory for creating SDTP sockets.
 * 
 * @author Lucas Baizer
 */
public class SDTPNetworkFactory extends NetworkFactory {
	SDTPNetworkFactory() {
	}

	@Override
	public Connection createConnection(String host, int port) throws UnknownHostException, IOException {
		try {
			return new SDTPConnection(host, port);
		} catch (CryptographyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Server createServer(int port, ClientConnectionListener l) throws ServerException {
		try {
			return new SDTPServer(port, (UDPConnectionListener) l);
		} catch (CryptographyException e) {
			throw new ServerException(e);
		}
	}
}
