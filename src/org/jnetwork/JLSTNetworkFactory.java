package org.jnetwork;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jnetwork.listener.ClientConnectionListener;
import org.jnetwork.listener.TCPConnectionListener;

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
	public Server createServer(int port, ClientConnectionListener l) throws ServerException {
		try {
			return new JLSTServer(port, (TCPConnectionListener) l);
		} catch (CryptographyException e) {
			throw new ServerException(e);
		}
	}
}
