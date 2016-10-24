package org.jnetwork;

import java.io.IOException;
import java.net.SocketException;
import java.security.Security;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import org.jnetwork.listener.TCPConnectionListener;

import com.sun.net.ssl.internal.ssl.Provider;

/**
 * An SSL representation of the Server object. Used for sending and receiving
 * data with SSLConnection objects.
 * 
 * @author Lucas Baizer
 */
public class SSLServer extends TCPServer {
	public SSLServer(Keystore keystore, int port, TCPConnectionListener clientSocketThread) {
		super(port, clientSocketThread);

		Security.addProvider(new Provider());

		System.setProperty("javax.net.ssl.keyStore", keystore.getKeystoreLocation().getPath());
		System.setProperty("javax.net.ssl.trustStore", keystore.getKeystoreLocation().getPath());
		System.setProperty("javax.net.ssl.keyStorePassword", keystore.getPassword());
	}

	@Override
	public void start() throws IOException {
		server = SSLServerSocketFactory.getDefault().createServerSocket(getBoundPort());

		super.startDispatch();
	}

	@Override
	protected void launchNewThread() throws IOException, InterruptedException {
		final SSLSocket client;
		try {
			client = (SSLSocket) ((SSLServerSocket) server).accept();
		} catch (SocketException e) {
			if (e.getMessage().equals("socket closed")) {
				return;
			}
			throw e;
		}

		final SocketPackage event = new SocketPackage(new SSLConnection(client));
		super.launchThreadForConnectedClient(event);
	}
}
