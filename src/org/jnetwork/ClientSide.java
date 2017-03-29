package org.jnetwork;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;

public class ClientSide extends CommonSide {
	private static final long serialVersionUID = 4323680714979039861L;

	protected transient Connection client;

	public ClientSide(Protocol protocol, String host, int port) throws IOException {
		client = protocol.getNetworkFactory().createConnection(host, port);

		Thread readingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					while (!client.isClosed()) {
						DataPackage in = (DataPackage) client.readObject();
						if (in.getMessage().equals("EXECUTE_METHOD")) {
							handleExecutionPacket(in);
						}
					}
				} catch (SocketException | EOFException e) {
					return;
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		readingThread.start();
	}

	public void invokeServer(String name, Serializable... args) throws IOException {
		client.writeObject(new DataPackage(this, name, args).setMessage("EXECUTE_METHOD"));
	}
}
