package org.jnetwork;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jnetwork.listener.TCPConnectionListener;

public class JLSTServer extends TCPServer {
	private SecurityService crypto;

	public JLSTServer(int i, TCPConnectionListener tcpConnectionListener) throws CryptographyException {
		super(i, tcpConnectionListener);

		crypto = SecurityService.generateRSASecurityService();
	}

	@Override
	protected void launchNewThread() throws IOException {
		final Socket socket;
		try {
			socket = server.accept();
		} catch (SocketException e) {
			if (e.getMessage().equals("socket closed")) {
				return;
			}
			throw e;
		}
		try {
			JLSTConnection client = new JLSTConnection(socket);
			client.writeUnencryptedObject(crypto.getPublicKey());

			DataPackage data = (DataPackage) client.readUnencryptedObject();

			SecretKey key = new SecretKeySpec(crypto.decrypt((byte[]) data.getObjects()[0]), "AES");
			client.getAESSecurityService().setPublicKey(key);
			client.getAESSecurityService().setPrivateKey(key);

			client.getAESSecurityService()
					.setParameters(new IvParameterSpec(crypto.decrypt((byte[]) data.getObjects()[1])));

			SocketPackage event = new SocketPackage(client);
			launchThreadForConnectedClient(event, "JLSTServer");
		} catch (CryptographyException | ClassNotFoundException e) {
			launchNewThread();
			throw new IOException(e);
		}
	}
}
