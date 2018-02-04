package example;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.jnetwork.CryptographyException;
import org.jnetwork.DataPackage;
import org.jnetwork.UDPConnection;
import org.jnetwork.UDPUtils;

/**
 * A JNetwork-level secure UDP connection. An explanation of how this works can
 * be found in the documentation for {@link SDTPServer}.
 * 
 * @author Lucas Baizer
 */
public class SDTPConnection extends UDPConnection implements SecureConnection {
	private SecurityService rsa;
	private SecurityService aes;

	public SDTPConnection(String host, int port) throws CryptographyException, IOException {
		super(host, port);
		bufferSize = 8192;

		try {
			aes = new SecurityService("AES/CBC/PKCS5Padding", true);

			KeyGenerator keyGen = KeyGenerator.getInstance("AES");
			keyGen.init(128);
			SecretKey aesKey = keyGen.generateKey();
			aes.setPublicKey(aesKey);
			aes.setPrivateKey(aesKey);
		} catch (Exception e) {
			throw new CryptographyException(e);
		}

		try {
			rsa = new SecurityService("RSA", false);

			writeUnencryptedObject("JNETWORK_SECURE_UDP_INITIATE_HANDSHAKE");

			DataPackage back = (DataPackage) readUnencryptedObject();
			PublicKey serverRSAPublicKey = (PublicKey) back.getObjects()[0];
			rsa.setPublicKey(serverRSAPublicKey);

			byte[] serial = UDPUtils.serializeObject(new DataPackage(rsa.encrypt(aes.getPrivateKey().getEncoded()),
					rsa.encrypt(aes.getParameters().getIV())));
			write(serial, 0, serial.length, null);
		} catch (ClassNotFoundException e) {
			throw new IOException(e);
		}
	}

	public SDTPConnection(DatagramSocket socket) throws CryptographyException {
		super(socket);

		aes = new SecurityService("AES/CBC/PKCS5Padding", true);
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

	@Override
	public void writeUnencryptedObject(Serializable obj) throws IOException {
		byte[] bytes = UDPUtils.serializeObject(obj);
		write(bytes, 0, bytes.length, null);
	}

	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException {
		write(bytes, offset, length, aes);
	}

	void write(byte[] bytes, int offset, int length, SecurityService crypto) throws IOException {
		try {
			byte[] b = bytes;
			if (crypto != null) {
				b = crypto.encrypt(bytes);
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
	public int read(byte[] arr, int off, int len) throws IOException {
		return read(arr, off, len, aes);
	}

	int read(byte[] arr, int off, int len, SecurityService crypto) throws IOException {
		DatagramPacket packet = new DatagramPacket(arr, off, len);
		socket.receive(packet);

		if (crypto != null) {
			try {
				packet.setData(crypto.decrypt(trim(packet.getData())));
			} catch (CryptographyException e) {
				throw new IOException("Failure reading bytes", e);
			}
		}
		
		return packet.getLength();
	}

	@Override
	public void readUnencrypted(byte[] arr) throws IOException {
		DatagramPacket packet = new DatagramPacket(arr, arr.length);
		socket.receive(packet);
	}

	@Override
	public Serializable readUnencryptedObject() throws IOException, ClassNotFoundException {
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
			packet.setData(aes.decrypt(trim(packet.getData())));
		} catch (CryptographyException e) {
			throw new IOException("Failure reading bytes", e);
		}
		packet.setData(Arrays.copyOf(packet.getData(), 8192));

		return packet;
	}

	byte[] trim(byte[] bytes) {
		int i = bytes.length - 1;
		while (i >= 0 && bytes[i] == 0) {
			--i;
		}

		return Arrays.copyOf(bytes, i + 1);
	}

	@Override
	public void writeUnencrypted(int v) throws IOException {
		write(new byte[] { (byte) v }, 0, 1, null);
	}

	@Override
	public void writeUnencrypted(byte[] arr, int off, int len) throws IOException {
		write(arr, off, len, null);
	}

	@Override
	public int readUnencrypted() throws IOException {
		byte[] arr = new byte[1];
		read(arr, 0, 1, null);
		return arr[0];
	}

	@Override
	public void readUnencrypted(byte[] arr, int off, int len) throws IOException {
		read(arr, off, len, null);
	}
}
