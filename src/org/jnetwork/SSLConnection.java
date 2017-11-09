package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.Security;
import java.util.Objects;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.sun.net.ssl.internal.ssl.Provider;

/**
 * An SSL representation of the Connection object. Used for writing and reading
 * data with an SSLServer.
 * 
 * @author Lucas Baizer
 */
public class SSLConnection extends TCPConnection implements SecureConnection {
	static {
		Security.addProvider(new Provider());
	}

	SSLConnection() {
	}

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

	@Override
	public void writeUnencrypted(int v) {
		throwUOE();
	}

	@Override
	public void writeUnencrypted(byte[] arr, int off, int len) {
		throwUOE();
	}

	@Override
	public void writeUnencryptedObject(Serializable obj) {
		throwUOE();
	}

	@Override
	public Serializable readUnencryptedObject() {
		return throwUOE();
	}

	@Override
	public int readUnencrypted() {
		return throwUOE();
	}

	@Override
	public void readUnencrypted(byte[] arr, int off, int len) {
		throwUOE();
	}

	private <T> T throwUOE() {
		throw new UnsupportedOperationException("Cannot perform unencrypted operating on an SSL connection");
	}
}
