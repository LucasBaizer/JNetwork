package org.jnetwork;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * An SSL representation of the Connection object. Used for writing and reading
 * data with an SSLServer.
 * 
 * @author Lucas Baizer
 */
public class SSLConnection extends TCPConnection {
	public SSLConnection(SSLSocket socket) throws IOException {
		super(socket);
	}

	public SSLConnection(String host, int port) throws UnknownHostException, IOException {
		this(host, port, true);
	}

	public SSLConnection(String host, int port, boolean connectNow) throws UnknownHostException, IOException {
		if (Objects.equals(host, null) || Objects.equals(port, null))
			throw new NullPointerException();

		if (connectNow) {
			this.connection = SSLSocketFactory.getDefault().createSocket(host, port);
			this.out = new AdvancedOutputStream(connection.getOutputStream());
			this.in = new AdvancedInputStream(connection.getInputStream());
		} else {
			this.address = new InetSocketAddress(host, port);
		}
	}

	@Override
	public void connect() throws UnknownHostException, IOException {
		this.connection = SSLSocketFactory.getDefault().createSocket(address.getHostString(), address.getPort());
		this.out = new AdvancedOutputStream(connection.getOutputStream());
		this.in = new AdvancedInputStream(connection.getInputStream());
	}
}
