package org.jnetwork;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketException;
import java.util.HashMap;

public class ServerSide extends CommonSide implements TCPConnectionCallback {
	private static final long serialVersionUID = 3133250598296571865L;

	protected transient TCPServer server;

	public ServerSide(int port) {
		server = new TCPServer(port, this);
	}

	public void start() throws IOException, InterruptedException {
		server.start();
		server.waitUntilClose();
		server.close();
	}

	private transient HashMap<String, ClientSide> threadSideMapping = new HashMap<>();
	private transient HashMap<String, TCPConnection> threadMapping = new HashMap<>();

	@Override
	public void clientConnected(ClientData data) {
		try {
			TCPConnection client = (TCPConnection) data.getConnection();
			threadMapping.put(Thread.currentThread().getName(), client);
			while (!client.isClosed()) {
				DataPackage in = (DataPackage) client.readObject();
				if (in.getMessage().equals("EXECUTE_METHOD")) {
					ClientSide clientSide = (ClientSide) in.getObjects()[0];
					threadSideMapping.put(Thread.currentThread().getName(), clientSide);
					handleExecutionPacket(in);
				}
			}
		} catch (SocketException | EOFException e) {
			return;
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void invokeClient(String name, Object... args) throws IOException {
		threadMapping.get(Thread.currentThread().getName()).getOutputStream()
				.writeUnshared(new DataPackage(threadSideMapping.get(Thread.currentThread().getName()), name, args)
						.setMessage("EXECUTE_METHOD"));
	}
}
