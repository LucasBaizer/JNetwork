package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.Key;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

/**
 * A JNetwork-level secure UDP connection. An explanation of how this works can
 * be found in the documentation for {@link SDTPServer}.
 * 
 * @author Lucas Baizer
 */
public class SDTPConnection extends UDPConnection {
	protected SecretKey aesKey;
	private SecurityService rsa;
	private SecurityService aes;

	public SDTPConnection(String host, int port) throws CryptographyException, IOException {
		super(host, port);
		bufferSize = 8192;

		try {
			aes = new SecurityService("AES/CBC/PKCS5Padding");
			aes.setUseParameters(true);

			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			aesKey = keyGen.generateKey();
		} catch (Exception e) {
			throw new CryptographyException(e);
		}

		try {
			rsa = new SecurityService("RSA");

			writeUnencryptedObject("JNETWORK_SECURE_UDP_INITIATE_HANDSHAKE");

			DataPackage back = (DataPackage) readUnencryptedObject();
			PublicKey serverRSAPublicKey = (PublicKey) back.getObjects()[0];
			rsa.setPublicKey(serverRSAPublicKey);

			byte[] keyBytes = aesKey.getEncoded();
			write(keyBytes, 0, keyBytes.length, rsa, rsa.getPublicKey());

			byte[] paramsBytes = aes.getParameters().getIV();
			write(paramsBytes, 0, paramsBytes.length, rsa, rsa.getPublicKey());
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public SDTPConnection(DatagramSocket socket) throws CryptographyException {
		super(socket);

		aes = new SecurityService("AES/CBC/PKCS5Padding");
		aes.setUseParameters(true);
	}

	@Override
	public void setBufferSize(int size) {
		if (size < 8192) {
			throw new IndexOutOfBoundsException(size + " < 8192");
		}
		super.setBufferSize(size);
	}

	SecurityService getAESSecurityService() {
		return aes;
	}

	void writeUnencryptedObject(Serializable obj) throws IOException {
		byte[] bytes = UDPUtils.serializeObject(obj);
		write(bytes, 0, bytes.length, null, null);
	}

	/**
	 * Encrypts and then writes an array of bytes. This array should <b>not</b>
	 * be encrypted when it is passed as a parameter. It will be encrypted by
	 * the method.
	 */
	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException {
		write(bytes, offset, length, aes, aesKey);
	}

	void write(byte[] bytes, int offset, int length, SecurityService crypto, Key key) throws IOException {
		try {
			byte[] b = bytes;
			if (crypto != null) {
				b = crypto.encrypt(bytes, key);
			}
			socket.send(new DatagramPacket(b, offset, b.length, targetAddress));
		} catch (CryptographyException e) {
			throw new IOException("Failure writing bytes", e);
		}
	}

	/**
	 * Reads AES-encrypted bytes into a buffer, and then decrypts them.
	 */
	@Override
	public void read(byte[] arr, int off, int len) throws IOException {
		read(arr, off, len, aes, aesKey);
	}

	void read(byte[] arr, int off, int len, SecurityService crypto, Key key) throws IOException {
		DatagramPacket packet = new DatagramPacket(arr, off, len);
		socket.receive(packet);

		try {
			packet.setData(crypto.decrypt(trim(packet.getData()), key));
		} catch (CryptographyException e) {
			throw new IOException("Failure reading bytes", e);
		}
	}

	void readUnencrypted(byte[] arr) throws IOException {
		DatagramPacket packet = new DatagramPacket(arr, arr.length);
		socket.receive(packet);
	}

	Serializable readUnencryptedObject() throws IOException, ClassNotFoundException {
		byte[] arr = new byte[bufferSize];
		readUnencrypted(arr);
		return UDPUtils.deserializeObject(arr);
	}

	@Override
	protected DatagramPacket readPacket() throws IOException {
		byte[] receive = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(receive, receive.length);
		socket.receive(packet);
		try {
			packet.setData(aes.decrypt(trim(packet.getData()), aesKey));
		} catch (CryptographyException e) {
			throw new IOException("Failure reading bytes", e);
		}

		return packet;
	}

	byte[] trim(byte[] bytes) {
		int i = bytes.length - 1;
		while (i >= 0 && bytes[i] == 0) {
			--i;
		}

		return Arrays.copyOf(bytes, i + 1);
	}
}
