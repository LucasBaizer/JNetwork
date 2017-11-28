package example;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.PublicKey;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.jnetwork.CryptographyException;
import org.jnetwork.DataPackage;
import org.jnetwork.TCPConnection;

/**
 * A JNetwork-level secure TCP connection. An explanation of how this works can
 * be found in the documentation for {@link JLSTServer}.
 * 
 * @author Lucas Baizer
 */
public class JLSTConnection extends TCPConnection implements SecureConnection {
	private SecurityService rsa;
	private SecurityService aes;

	public JLSTConnection(String host, int port) throws UnknownHostException, IOException, CryptographyException {
		super(host, port);

		SecretKey aesKey = null;
		try {
			aes = new SecurityService("AES/CBC/PKCS5Padding", true);

			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			aesKey = keyGen.generateKey();

			aes.setPublicKey(aesKey);
			aes.setPrivateKey(aesKey);
		} catch (Exception e) {
			throw new CryptographyException(e);
		}

		try {
			rsa = new SecurityService("RSA", false);

			PublicKey serverRSAPublicKey = (PublicKey) readUnencryptedObject();
			rsa.setPublicKey(serverRSAPublicKey);

			writeUnencryptedObject(
					new DataPackage(rsa.encrypt(aesKey.getEncoded()), rsa.encrypt(aes.getParameters().getIV())));
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public JLSTConnection(Socket socket) throws IOException, CryptographyException {
		super(socket);

		aes = new SecurityService("AES/CBC/PKCS5Padding", true);
	}

	SecurityService getAESSecurityService() {
		return aes;
	}

	@Override
	public void writeUnencryptedObject(Serializable obj) throws IOException {
		getObjectOutputStream().writeObject(obj);
	}

	@Override
	public Serializable readUnencryptedObject() throws ClassNotFoundException, IOException {
		return (Serializable) getObjectInputStream().readObject();
	}

	@Override
	public void writeUnencrypted(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void writeUnencrypted(byte[] bytes, int off, int len) throws IOException {
		out.write(bytes, off, len);
	}

	@Override
	public void write(int b) throws IOException {
		try {
			out.write(aes.encrypt(new byte[] { (byte) b })[0]);
		} catch (CryptographyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		try {
			out.write(aes.encrypt(bytes), off, len);
		} catch (CryptographyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int read() throws IOException {
		try {
			return aes.decrypt(new byte[] { (byte) getObjectInputStream().read() })[0];
		} catch (CryptographyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void read(byte[] arr, int off, int len) throws IOException {
		try {
			getObjectInputStream().read(aes.decrypt(arr), off, len);
		} catch (CryptographyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int readUnencrypted() throws IOException {
		return getObjectInputStream().read();
	}

	@Override
	public void readUnencrypted(byte[] arr, int off, int len) throws IOException {
		getObjectInputStream().read(arr, off, len);
	}
}
