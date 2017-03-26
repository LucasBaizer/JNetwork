package org.jnetwork;
import java.io.EOFException;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketException;
import java.util.HashMap;

import org.jnetwork.ClientData;
import org.jnetwork.DataPackage;
import org.jnetwork.TCPConnection;
import org.jnetwork.TCPConnectionCallback;
import org.jnetwork.TCPServer;

public class ServerSide extends CommonSide implements TCPConnectionCallback, Serializable {
	private static final long serialVersionUID = 3133250598296571865L;

	private transient TCPServer server;

	public ServerSide(int port) {
		server = new TCPServer(port, this);
	}

	public void start() throws IOException, InterruptedException {
		server.start();
		server.waitUntilClose();
		server.close();
	}

	private HashMap<String, ClientSide> threadMapping = new HashMap<>();

	@Override
	public void clientConnected(ClientData data) {
		try {
			TCPConnection client = (TCPConnection) data.getConnection();
			while (!client.isClosed()) {
				DataPackage in = (DataPackage) client.readObject();
				if (in.getMessage().equals("EXECUTE_METHOD")) {
					ClientSide clientSide = (ClientSide) in.getObjects()[0];
					threadMapping.put(Thread.currentThread().getName(), clientSide);
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
		for (ClientData client : server.getClients()) {
			if (client.getHoldingThread().getName().equals(Thread.currentThread().getName())) {
				((TCPConnection) client.getConnection()).getOutputStream().writeUnshared(
						new DataPackage(threadMapping.get(client.getHoldingThread().getName()), name, args)
								.setMessage("EXECUTE_METHOD"));
			}
		}
	}
}
