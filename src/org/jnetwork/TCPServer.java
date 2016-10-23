package org.jnetwork;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.jnetwork.listener.TCPConnectionListener;

/**
 * A TCP representation of the Server object. Used for sending and receiving
 * data with TCPConnection objects.
 * 
 * @author Lucas Baizer
 */
public class TCPServer extends Server {
	private ServerSocket server;

	public TCPServer(int port, TCPConnectionListener clientSocketThread) throws IOException {
		this(port, Integer.MAX_VALUE, clientSocketThread);
	}

	public TCPServer(int port, int maxClients, TCPConnectionListener clientSocketThread) throws IOException {
		super(port, maxClients, clientSocketThread);
	}

	@Override
	public void start() throws IOException {
		this.server = new ServerSocket(getBoundPort());

		super.startDispatch();
	}

	@Override
	protected void launchNewThread() throws IOException, InterruptedException {
		final Socket client;
		try {
			client = server.accept();
		} catch (SocketException e) {
			if (e.getMessage().equals("socket closed")) {
				return;
			} else {
				throw e;
			}
		}
		// wait until a client disconnects if the maximum amount of
		// clients is full
		// TODO synchronize around object to not chew up CPU
		while (clients.size() == getMaxClients()) {
			Thread.sleep(20);
		}
		final SocketPackage event = new SocketPackage(new TCPConnection(client));

		refresh();
		clients.add(event);
		refresh();

		// sets saved data
		for (SavedData data : savedData)
			if (event.getConnection().getRemoteSocketAddress().toString()
					.equals(data.pkg.getConnection().getRemoteSocketAddress().toString()))
				event.setExtraData(data.data);

		Thread thr = new Thread(new Runnable() {
			@Override
			public void run() {
				((TCPConnectionListener) getClientConnectionListener()).clientConnected(event);
				try {
					removeClient(event);
				} catch (IOException e) {
					Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				}
			}
		});
		event.setHoldingThread(thr);
		thr.setName("JNetwork-TCPServer-Thread-" + client.getRemoteSocketAddress());
		thr.start();

		launchNewThread();
	}

	@Override
	public void close() throws IOException {
		server.close();
		super.close();
	}

	/**
	 * Gets the internal <code>ServerSocket</code> the <code>Server</code> is
	 * built off of.
	 * 
	 * @return <b>ServerSocket</b> - The internal <code>ServerSocket</code> the
	 *         <code>Server</code> is built off of.
	 * 
	 * @see java.net.ServerSocket
	 */
	public ServerSocket getServerSocket() {
		return server;
	}
}
