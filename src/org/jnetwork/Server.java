package org.jnetwork;

import java.io.Closeable;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * A server-side utility used for easily handling an infinite amount of
 * connections between clients. When the constructor is called, a
 * <code>ServerSocket</code> is bound to a given port and then created. A new
 * thread is then launched, which will wait for a client to connect, and when
 * they do, a new <code>ClientData</code> will be created containing all the
 * information required for the connection with the client. In another new
 * thread, launched from the new thread initially launched, the
 * <code>ClientConnectionCallback.clientConnected</code> will be called and
 * parameterized with the <code>ClientData</code>. <br>
 * <br>
 * Whenever the <code>Server</code> is asked to run any function that deals with
 * the connected clients, it will call {@link Server#refresh()}. For each closed
 * client that that is still in the <code>Server</code>'s {@code List
 * <ClientData>}, the client will be removed, and every
 * <code>RefreshListener.clientDisconnect</code> added with
 * {@link Server#addRefreshListener(RefreshListener)} will be called and
 * parameterized with the client's <code>ClientData</code>. <br>
 * <br>
 * When {@link Server#removeClient(ClientData)} is called, every
 * <code>ClientDisconnectionCallback</code> added with
 * {@link Server#addClientDisconnectionListener(ClientDisconnectionCallback)}
 * will be called and parameterized with the client's <code>ClientData</code> .
 * <br>
 * <br>
 * 
 * @see java.net.ServerSocket
 * @see org.jnetwork.ClientData
 * @see org.jnetwork.listener.network.event.RefreshListener
 * @see org.jnetwork.ClientConnectionCallback.event.ClientConnectionListener
 * @see org.jnetwork.ClientDisconnectionCallback.ClientDisconnectionListener.event.
 *      ClientRemovedListener
 * @see org.jnetwork.ClientData
 * 
 * @author Lucas Baizer
 */
public abstract class Server implements Closeable {
	private ClientConnectionCallback thread;
	private int port;
	protected int capacity = -1;
	protected ArrayList<ClientData> clients = new ArrayList<ClientData>();
	private ArrayList<ClientDisconnectionCallback> removers = new ArrayList<ClientDisconnectionCallback>();
	protected ConnectionHandler<?> connectionHandler;
	private boolean started;
	private Object closeWaiter = new Object();
	protected ExceptionCallback exceptionCallback;

	/**
	 * Constructs a new <code>Server</code> and starts a new
	 * <code>ServerSocket</code> on port <code>port</code>.
	 * 
	 * @param port
	 *            - The port to start the server on.
	 * @param clientSocketThread
	 *            - The <code>ClientConnectionCallback<code> to be called when a
	 *            client connects. It will be given the client's <code>Socket</code>
	 *            and an instance of <code>this</code>.
	 */
	public Server(int port, ClientConnectionCallback clientSocketThread) {
		this.thread = clientSocketThread;
		this.port = port;
	}

	public Server setExceptionCallback(ExceptionCallback back) {
		this.exceptionCallback = back;
		return this;
	}

	/**
	 * Starts the dispatch thread.
	 * 
	 * @throws ServerException
	 *             If the server has already been started.
	 */
	private Thread dispatcher;

	protected void startDispatch() throws ServerException {
		if (started)
			throw new ServerException("Server already started");

		started = true;

		dispatcher = new Thread(getDispatcher(), "JNetwork-Server-Accept-Dispatcher");
		dispatcher.start();
	}

	private Runnable getDispatcher() {
		return new Runnable() {
			@Override
			public void run() {
				try {
					launchNewThread();
				} catch (Exception e) {
					if (exceptionCallback != null) {
						exceptionCallback.exceptionThrown(e);
						dispatcher = new Thread(getDispatcher());
						dispatcher.start();
					} else {
						Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(),
								e);
					}
				}
			}
		};
	}

	public void setCapacity(int cap) {
		clients.ensureCapacity(cap);
		this.capacity = cap;
	}

	public int getCapacity() {
		return this.capacity;
	}

	/**
	 * Starts the server, so that clients can connect to it.
	 * 
	 * @throws IOException
	 *             If an error occurs while starting the server.
	 */
	public abstract void start() throws IOException;

	/**
	 * Adds a <code>ClientRemoveListener</code> to be called on when a client is
	 * removed by <b><code>removeClient(ClientData client)</code></b>
	 * 
	 * @param listener
	 *            - The listener to be removed.
	 * 
	 * @throws NullPointerException
	 *             If <code>listener</code> is null.
	 */
	public void addClientDisconnectionListener(ClientDisconnectionCallback listener) {
		removers.add(listener);
	}

	/**
	 * Removes a <code>ClientDisconnectionCallback</code> to be called on when a
	 * client is removed by <b> <code>removeClient(ClientData event)</code></b>
	 * 
	 * @param listener
	 *            - The listener to be added.
	 * 
	 * @return boolean - If the listener was added in the first place.
	 * 
	 * @throws NullPointerException
	 *             If <code>listener</code> is null.
	 */
	public boolean removeClientDisconnectionListener(ClientDisconnectionCallback listener) {
		return removers.remove(listener);
	}

	/**
	 * Gets the port the <code>ServerSocket</code> was bound to.
	 * 
	 * @return int - The port.
	 */
	public int getBoundPort() {
		return this.port;
	}

	/**
	 * Gets the addresses of each connected client.
	 * 
	 * @return SocketAddress[] - The array containing the client's addresses.
	 */
	public SocketAddress[] getClientsAddresses() {
		refresh();
		SocketAddress[] addresses = new SocketAddress[clients.size()];
		for (int i = 0; i < clients.size(); i++) {
			addresses[i] = clients.get(i).getConnection().getRemoteSocketAddress();
		}

		return addresses;
	}

	/**
	 * Gets every <code>ClientDisconnectionCallback</code> added with <b>
	 * <code>addClientDisconnectionListener(ClientDisconnectionCallback)</code>
	 * </b>.
	 * 
	 * @return ClientDisconnectionCallback[] - The array containing the
	 *         ClientRemovedListeners.
	 */
	public ClientDisconnectionCallback[] getClientDisconnectionListeners() {
		return removers.toArray(new ClientDisconnectionCallback[removers.size()]);
	}

	/**
	 * Removes the client from the server's list of clients and closes it if it
	 * was not already closed.
	 * 
	 * @param client
	 *            - The ClientData of the client.
	 * 
	 * @returns boolean - If the client was actually in the server's
	 *          {@code List <ClientData> }
	 * @throws IOException
	 *             If there is an error closing the client if it is not already
	 *             closed.
	 */
	public void removeClient(ClientData client) throws IOException {
		if (!client.isKeepAlive() && clients.contains(client)) {
			clients.remove(client);

			refresh();
			if (!client.getConnection().isClosed())
				client.getConnection().close();
			if (client.getHoldingThread().isAlive())
				client.getHoldingThread().interrupt();

			for (ClientDisconnectionCallback listener : removers) {
				listener.clientDisconnected(client);
			}

			refresh();
		}
	}

	/**
	 * Removes the client from the server's list of clients and closes it if it
	 * was not already closed.
	 * 
	 * @param addr
	 *            - The client's address.
	 * 
	 * @returns boolean - If the client was actually in the server's
	 *          {@code List <ClientData> }
	 * 
	 * @throws IOException
	 *             If there is an error closing the client if it is not already
	 *             closed.
	 */
	public void removeClient(SocketAddress addr) throws IOException {
		removeClient(getClient(addr));
	}

	/**
	 * Gets a connected client's <code>ClientData</code> by their IP address.
	 * 
	 * @param addr
	 *            - The <code>SocketAddress</code> of the client.
	 * 
	 * @return ClientData - If a user has the same <code>SocketAddress</code> as
	 *         the <code>addr</code> parameter.<br>
	 *         null - If no <code>ClientData</code> is found with the same
	 *         <code>SocketAddress</code> as the <code>addr</code> parameter.
	 */
	public ClientData getClient(SocketAddress addr) {
		for (ClientData evt : clients) {
			if (evt.getConnection().getRemoteSocketAddress().toString().equals(addr.toString()))
				return evt;
		}
		return null;
	}

	/**
	 * Gets a connected client's <code>ClientData</code> by their
	 * <code>Socket</code>.
	 * 
	 * @param socket
	 *            - The <code>Connection</code> of the client.
	 * @return ClientData - If a user has the same <code>Socket</code> as the
	 *         <code>socket</code> parameter. <br>
	 *         null - If no <code>ClientData</code> is found with the same
	 *         <code>Socket</code> as the <code>socket</code> parameter.
	 * 
	 * @see org.jnetwork.ClientData
	 */
	public ClientData getClient(Connection socket) {
		for (ClientData evt : clients) {
			if (evt.getConnection().equals(socket))
				return evt;
		}
		return null;
	}

	/**
	 * Removes all of the closed clients that are still in the server's internal
	 * {@code List<ClientData>}.
	 **/
	public void refresh() {
		ArrayList<ClientData> closedClients = new ArrayList<>();

		for (ClientData ce : clients) {
			Connection client = ce.getConnection();
			if (client.isClosed()) {
				closedClients.add(ce);
			}
		}

		// prevent CME
		for (ClientData closedClient : closedClients) {
			clients.remove(closedClient);
		}
	}

	/**
	 * Gets the <code>Socket</code> of every connected client.
	 * 
	 * @return Socket[] - The array containing the <code>Socket</code> of every
	 *         connected client.
	 * 
	 * @see java.net.Socket
	 */
	public ClientData[] getClients() {
		return clients.toArray(new ClientData[clients.size()]);
	}

	/**
	 * Causes the current thread to block until the server is closed.
	 * 
	 * @throws InterruptedException
	 *             Specified by {@link Object#wait()}.
	 * 
	 * @see #getServerSocket()
	 */
	public void waitUntilClose() throws InterruptedException {
		synchronized (closeWaiter) {
			closeWaiter.wait();
		}
	}

	/**
	 * Closes the <code>ServerSocket</code>.
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		synchronized (closeWaiter) {
			closeWaiter.notifyAll();
		}
	}

	/**
	 * Returns the port the <code>ServerSocket</code> was initialized on in the
	 * form of a <code>String</code>.
	 */
	@Override
	public String toString() {
		return Integer.toString(port);
	}

	/**
	 * Launches a new thread so a new client can connect.
	 * 
	 * @throws IOException
	 * @throws InterruptedException
	 */
	protected abstract void launchNewThread() throws IOException, InterruptedException;

	/**
	 * @return the ClientConnectionCallback specified at instantiation.
	 */
	public ClientConnectionCallback getClientConnectionListener() {
		return thread;
	}

	public ConnectionHandler<?> getConnectionHandler() {
		return connectionHandler;
	}

	public void setConnectionHandler(ConnectionHandler<?> connectionHandler) {
		this.connectionHandler = connectionHandler;
	}
}