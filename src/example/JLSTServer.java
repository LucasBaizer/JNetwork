package example;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.nio.file.Files;
import java.security.KeyStore;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jnetwork.ClientData;
import org.jnetwork.CryptographyException;
import org.jnetwork.DataPackage;
import org.jnetwork.Keystore;
import org.jnetwork.SecureServer;
import org.jnetwork.TCPConnectionCallback;
import org.jnetwork.TCPServer;

/**
 * A JNetwork-level secure TCP server. When a client initially connects, a
 * handshake is performed between the client and the server. The handshake works
 * like so:<br>
 * <p>
 * 1. A client connects to the server.<br>
 * 2. The server sends its public RSA key to the client.<br>
 * 3. The client generates a symmetric AES key, encrypts it with the public RSA
 * key sent by the server, and sends it to the server.<br>
 * 4. The server decrypts the RSA-encrypted AES key using the public RSA key's
 * paired private key.<br>
 * 5. The handshake is complete.<br>
 * <p>
 * After the handshake is complete, the AES key is used for encrypting and
 * decrypting when writeXXX and readXXX are called on the Connection object.
 * <b>It is neccessary to call writeXXX and readXXX on the Connection object,
 * not the Connection's input/output streams.</b>
 * 
 * @author Lucas Baizer
 */
public class JLSTServer extends TCPServer implements SecureServer {
	private Keystore keystore;
	private SecurityService crypto;

	public JLSTServer(int i, TCPConnectionCallback tcpConnectionListener, Keystore keystore)
			throws CryptographyException {
		super(i, tcpConnectionListener);

		if (keystore != null) {
			setKeystore(keystore);
		}
	}

	public JLSTServer(int i, TCPConnectionCallback tcpConnectionListener) throws CryptographyException {
		super(i, tcpConnectionListener);

		crypto = SecurityService.generateRSASecurityService();
	}

	public void setKeystore(Keystore keystore) throws CryptographyException {
		try {
			KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
			keyStore.load(Files.newInputStream(keystore.getKeystoreFile().toPath()), keystore.getPasswordArray());

			crypto = new SecurityService("RSA", false);
			crypto.setPublicKey(keyStore.getCertificate(keystore.getAlias()).getPublicKey());
			crypto.setPrivateKey(keyStore.getKey(keystore.getAlias(), keystore.getKeyPasswordArray()));

			this.keystore = keystore;
		} catch (Exception e) {
			throw new CryptographyException(e);
		}
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

			ClientData event = new ClientData(client);
			launchThreadForConnectedClient(event, "JLSTServer");
		} catch (CryptographyException | ClassNotFoundException e) {
			launchNewThread();
			throw new IOException(e);
		}
	}

	@Override
	public Keystore getKeystore() {
		return keystore;
	}

	@Override
	public void useRandomKeystore() throws CryptographyException {
		crypto = SecurityService.generateRSASecurityService();
	}
}
