package org.jnetwork;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

/**
 * A TCP representation of the Server object. Used for sending and receiving
 * data with TCPConnection objects.
 * 
 * @author Lucas Baizer
 */
public class TCPServer extends Server {
	protected ServerSocket server;
	protected InetAddress boundAddress;

	public TCPServer(int port, TCPConnectionCallback clientSocketThread) {
		super(port, clientSocketThread);
	}

	@Override
	public void start() throws IOException {
		if (boundAddress == null) {
			this.server = new ServerSocket(getBoundPort());
		} else {
			this.server = new ServerSocket(getBoundPort(), 0, boundAddress);
		}

		super.startDispatch();
	}

	@Override
	protected void launchNewThread() throws IOException {
		Socket client;
		try {
			client = server.accept();
		} catch (SocketException e) {
			if (e.getMessage().equals("socket closed")) {
				return;
			}
			throw e;
		}
		if (connectionHandler != null && !((ConnectionHandler<Socket>) connectionHandler).handle(this, client)) {
			client.close();
			launchNewThread();
		} else {
			if (capacity != -1 && capacity == clients.size()) {
				client.close();
				launchNewThread();
			} else {
				ClientData event = new ClientData(new TCPConnection(client));
				launchThreadForConnectedClient(event, "TCPServer");
			}
		}
	}

	protected void launchThreadForConnectedClient(ClientData event, String name) throws IOException {
		refresh();
		clients.add(event);
		refresh();

		Thread thr = new Thread(new Runnable() {
			@Override
			public void run() {
				((TCPConnectionCallback) getClientConnectionListener()).clientConnected(event);
				try {
					removeClient(event);
				} catch (IOException e) {
					if (exceptionCallback == null) {
						Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
								e);
					} else {
						exceptionCallback.exceptionThrown(e);
					}
				}
			}
		});
		event.setHoldingThread(thr);
		thr.setName("JNetwork-" + name + "-Thread-" + event.getConnection().getRemoteSocketAddress());
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

	public InetAddress getBoundAddress() {
		return boundAddress;
	}

	public void setBoundAddress(InetAddress boundAddress) {
		this.boundAddress = boundAddress;
	}
}
