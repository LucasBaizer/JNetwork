package org.jnetwork;

import java.io.IOException;
import java.net.Socket;
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

	static void ensureProvider() {
	}

	static void setStaticKeystore(Keystore keystore) {
		System.setProperty("javax.net.ssl.keyStore", keystore.getKeystoreFile().getAbsolutePath());
		System.setProperty("javax.net.ssl.trustStore", keystore.getKeystoreFile().getAbsolutePath());
		System.setProperty("javax.net.ssl.keyStorePassword", keystore.getPassword());
	}

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
		setStaticKeystore(keystore);
		this.keystore = keystore;
	}

	public Keystore getKeystore() {
		return this.keystore;
	}

	@Override
	public void start() throws IOException {
		if (boundAddress == null) {
			server = SSLServerSocketFactory.getDefault().createServerSocket(getBoundPort());
		} else {
			server = SSLServerSocketFactory.getDefault().createServerSocket(getBoundPort(), 0, boundAddress);
		}

		super.startDispatch();
	}

	@Override
	protected void launchNewThread() throws IOException {
		SSLSocket client;
		ClientData event;
		try {
			client = (SSLSocket) ((SSLServerSocket) server).accept();
			if (connectionHandler != null && !((ConnectionHandler<Socket>) connectionHandler).handle(this, client)) {
				client.close();
				launchNewThread();
			} else {
				if (capacity != -1 && capacity == clients.size()) {
					client.close();
					launchNewThread();
				} else {
					event = new ClientData(new SSLConnection(client));
					super.launchThreadForConnectedClient(event, "SSLServer");
				}
			}
		} catch (SocketException e) {
			if (e.getMessage().equals("socket closed")) {
				return;
			}
			throw e;
		}
	}

	@Override
	public void useRandomKeystore() {
		throw new UnsupportedOperationException("A keystore file must be specified for SSL");
	}
}
