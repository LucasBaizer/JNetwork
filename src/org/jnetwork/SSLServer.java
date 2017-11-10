package org.jnetwork;

import java.io.IOException;
import java.net.SocketException;
import java.security.Security;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import com.sun.net.ssl.internal.ssl.Provider;

/**
 * An SSL representation of the Server object. Used for sending and receiving
 * data with SSLConnection objects.
 * 
 * @author Lucas Baizer
 */
public class SSLServer extends TCPServer implements SecureServer {
	protected Keystore keystore;

	static {
		Security.addProvider(new Provider());
	}

	public SSLServer(int port, TCPConnectionCallback clientSocketThread, Keystore keystore) {
		super(port, clientSocketThread);

		if (keystore != null) {
			setKeystore(keystore);
		}
	}

	public void setKeystore(Keystore keystore) {
		System.setProperty("javax.net.ssl.keyStore", keystore.getKeystoreFile().getAbsolutePath());
		System.setProperty("javax.net.ssl.trustStore", keystore.getKeystoreFile().getAbsolutePath());
		System.setProperty("javax.net.ssl.keyStorePassword", keystore.getPassword());
		this.keystore = keystore;
	}

	public Keystore getKeystore() {
		return this.keystore;
	}

	@Override
	public void start() throws IOException {
		server = SSLServerSocketFactory.getDefault().createServerSocket(getBoundPort());

		super.startDispatch();
	}

	@Override
	protected void launchNewThread() throws IOException {
		final SSLSocket client;
		try {
			client = (SSLSocket) ((SSLServerSocket) server).accept();
		} catch (SocketException e) {
			if (e.getMessage().equals("socket closed")) {
				return;
			}
			throw e;
		}

		final ClientData event = new ClientData(new SSLConnection(client));
		super.launchThreadForConnectedClient(event, "SSLServer");
	}

	@Override
	public void useRandomKeystore() {
		throw new UnsupportedOperationException("A keystore file must be specified for SSL");
	}
}
