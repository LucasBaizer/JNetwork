package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.HashMap;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.jnetwork.listener.ClientDisconnectionListener;
import org.jnetwork.listener.UDPConnectionListener;

/**
 * A JNetwork-level secure UDP server. When a client initially connects, a
 * handshake is performed between the client and the server. The handshake works
 * like so:<br>
 * <p>
 * 1. A client connects to the server.<br>
 * 2. The server sends its public RSA key to the client.<br>
 * 3. The client generates a symmetric AES key, encrypts it with the public RSA
 * key sent by the server, and sends it to the server.<br>
 * 4. The server decrypts the RSA-encrypted AES key using the public RSA key's
 * paired private key, and stores the AES key in a map.<br>
 * 5. The handshake is complete.<br>
 * <p>
 * Once the client sends data after the handshake occurs, the server uses the
 * AES key stored in the map to decrypt the data that the client has sent, and
 * to encrypt data that will be sent to the client.
 * 
 * @author Lucas Baizer
 */
public class SDTPServer extends UDPServer {
	private SecurityService crypto;

	public SDTPServer(int port, UDPConnectionListener clientSocketThread) throws CryptographyException {
		super(port, clientSocketThread);

		setBufferSize(8192);

		addClientDisconnectionListener(new ClientDisconnectionListener() {
			@Override
			public void clientDisconnected(SocketPackage event) {
				handshakeData.remove(event.getConnection().getRemoteSocketAddress());
			}
		});

		init();
	}

	private void init() throws CryptographyException {
		try {
			crypto = SecurityService.generateRSASecurityService();
		} catch (Exception e) {
			throw new CryptographyException(e);
		}
	}

	private static class HandshakeData implements Serializable {
		private static final long serialVersionUID = -3810794361727853185L;
		private SecretKey key;
		private IvParameterSpec parameters;

		public HandshakeData(SecretKey key, IvParameterSpec params) {
			this.key = key;
			this.parameters = params;
		}

		public SecretKey getKey() {
			return key;
		}

		public IvParameterSpec getParameters() {
			return parameters;
		}
	}

	private HashMap<SocketAddress, HandshakeData> handshakeData = new HashMap<>();

	@SuppressWarnings("resource")
	@Override
	protected void launchNewThread() throws IOException, InterruptedException {
		byte[] receiveData = new byte[bufferSize];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		server.receive(receivePacket); // initial package (new DataPackage())

		try {
			SDTPConnection conn = new SDTPConnection(server);
			conn.setBufferSize(8192);
			conn.setTargetAddress((InetSocketAddress) receivePacket.getSocketAddress());

			boolean handshake = false;
			try {
				Object obj = UDPUtils.deserializeObject(receivePacket.getData());

				if (obj instanceof String) {
					if (((String) obj).equals("JNETWORK_SECURE_UDP_INITIATE_HANDSHAKE")) {
						handshake = true;
					}
				}
			} catch (NullPointerException | IOException e) {
				// continue;
			}

			if (handshake) {
				/* start handshake */
				conn.writeUnencryptedObject(new DataPackage(crypto.getPublicKey()));

				byte[] read = new byte[256];
				conn.readUnencrypted(read); // read AES symmetric key
				read = crypto.decrypt(read);

				byte[] aesKey = read.clone();

				read = new byte[256];
				conn.readUnencrypted(read); // read IV parameters
				read = crypto.decrypt(read);

				byte[] iv = read.clone();

				handshakeData.put(receivePacket.getSocketAddress(),
						new HandshakeData(new SecretKeySpec(aesKey, "AES"), new IvParameterSpec(iv)));

				/* finished handshake */
				launchNewThread();
				return;
			}

			HandshakeData data = handshakeData.get(receivePacket.getSocketAddress());
			conn.aesKey = data.getKey();
			conn.getAESSecurityService().setParameters(data.getParameters());

			final SocketPackage event = new SocketPackage(conn);

			refresh();
			clients.add(event);
			refresh();

			Thread thr = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						((UDPConnectionListener) getClientConnectionListener()).dataReceived(event,
								conn.getAESSecurityService().decrypt(conn.trim(receivePacket.getData()), conn.aesKey));
					} catch (CryptographyException e1) {
						e1.printStackTrace();
					}
					try {
						removeClient(event);
					} catch (IOException e) {
						Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
								e);
					}
				}
			}, "JNetwork-SDTPServer-Thread-" + receivePacket.getSocketAddress());
			event.setHoldingThread(thr);
			thr.start();
		} catch (Exception e1) {
			throw new IOException(e1);
		}

		launchNewThread();
	}
}
