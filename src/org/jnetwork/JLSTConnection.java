package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;

/**
 * A JNetwork-level secure TCP connection. An explanation of how this works can
 * be found in the documentation for {@link JLSTServer}.
 * 
 * @author Lucas Baizer
 */
public class JLSTConnection extends TCPConnection {
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

	/**
	 * Writes an object without encrypting it.
	 * 
	 * @param obj
	 *            - The object to send.
	 * @throws IOException
	 *             If an error occurs while writing the object.
	 */
	public void writeUnencryptedObject(Serializable obj) throws IOException {
		out.writeObject(obj);
	}

	/**
	 * Reads an object without decrypting it.
	 * 
	 * @return The undecrypted object.
	 * @throws ClassNotFoundException
	 *             If the class of the object cannot be found.
	 * @throws IOException
	 *             If an error occurs while reading the object.
	 */
	public Serializable readUnencryptedObject() throws ClassNotFoundException, IOException {
		return (Serializable) in.readObject();
	}

	public void writeUnencrypted(int b) throws IOException {
		out.write(b);
	}

	public void writeUnencrypted(byte[] bytes) throws IOException {
		writeUnencrypted(bytes, 0, bytes.length);
	}

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
	public void writeObject(Serializable obj) throws IOException {
		try {
			out.writeObject(new SealedObject(obj, aes.getEncryptionCipher()));
		} catch (IllegalBlockSizeException e) {
			throw new IOException(e);
		}
	}

	@Override
	public int read() throws IOException {
		try {
			return aes.decrypt(new byte[] { (byte) in.read() })[0];
		} catch (CryptographyException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void read(byte[] arr, int off, int len) throws IOException {
		try {
			in.read(aes.decrypt(arr), off, len);
		} catch (CryptographyException e) {
			throw new IOException(e);
		}
	}

	public int readUnencrypted() throws IOException {
		return in.read();
	}

	public void readUnencrypted(byte[] arr) throws IOException {
		readUnencrypted(arr, 0, arr.length);
	}

	public void readUnencrypted(byte[] arr, int off, int len) throws IOException {
		in.read(arr, off, len);
	}

	@Override
	public Serializable readObject() throws ClassNotFoundException, IOException {
		try {
			return (Serializable) ((SealedObject) in.readObject()).getObject(aes.getPrivateKey());
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}

	@Override
	public void writeUnshared(Serializable obj) throws IOException {
		try {
			out.writeUnshared(new SealedObject(obj, aes.getEncryptionCipher()));
		} catch (IllegalBlockSizeException e) {
			throw new IOException(e);
		}
	}

	@Override
	public Serializable readUnshared() throws IOException, ClassNotFoundException {
		try {
			return (Serializable) ((SealedObject) in.readUnshared()).getObject(aes.getPrivateKey());
		} catch (InvalidKeyException | NoSuchAlgorithmException e) {
			throw new IOException(e);
		}
	}
}
