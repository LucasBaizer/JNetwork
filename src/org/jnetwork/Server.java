package org.jnetwork;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

/**
 * A server-side utility used for easily handling an infinite amount of
 * connections between clients. When the constructor is called, a
 * <code>ServerSocket</code> is bound to a given port and then created. A new
 * thread is then launched, which will wait for a client to connect, and when
 * they do, a new <code>SocketPackage</code> will be created containing all the
 * information required for the connection with the client. In another new
 * thread, launched from the new thread initially launched, the
 * <code>ClientConnectionCallback.clientConnected</code> will be called and
 * parameterized with the <code>SocketPackage</code>. <br>
 * <br>
 * Whenever the <code>Server</code> is asked to run any function that deals with
 * the connected clients, it will call {@link Server#refresh()}. For each closed
 * client that that is still in the <code>Server</code>'s {@code List
 * <SocketPackage>}, the client will be removed, and every
 * <code>RefreshListener.clientDisconnect</code> added with
 * {@link Server#addRefreshListener(RefreshListener)} will be called and
 * parameterized with the client's <code>SocketPackage</code>. <br>
 * <br>
 * When {@link Server#removeClient(SocketPackage)} is called, every
 * <code>ClientDisconnectionCallback</code> added with
 * {@link Server#addClientDisconnectionListener(ClientDisconnectionCallback)}
 * will be called and parameterized with the client's <code>SocketPackage</code>
 * . <br>
 * <br>
 * 
 * @see java.net.ServerSocket
 * @see org.jnetwork.SocketPackage
 * @see org.jnetwork.listener.network.event.RefreshListener
 * @see org.jnetwork.ClientConnectionCallback.event.ClientConnectionListener
 * @see org.jnetwork.ClientDisconnectionCallback.ClientDisconnectionListener.event.
 *      ClientRemovedListener
 * @see org.jnetwork.SocketPackage
 * 
 * @author Lucas Baizer
 */
public abstract class Server implements Closeable {
	private ClientConnectionCallback thread;
	private int port;
	protected ArrayList<SocketPackage> clients = new ArrayList<SocketPackage>();
	private ArrayList<ClientDisconnectionCallback> removers = new ArrayList<ClientDisconnectionCallback>();
	private boolean started;
	private Object closeWaiter = new Object();

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
	 * 
	 * @throws NullPointerException
	 *             If <code>clientSocketThread</code> is null.
	 */
	public Server(int port, ClientConnectionCallback clientSocketThread) {
		if (clientSocketThread == null)
			throw new NullPointerException("ClientConnectionCallback is null");

		this.thread = clientSocketThread;
		this.port = port;
	}

	/**
	 * Starts the dispatch thread.
	 * 
	 * @throws ServerException
	 *             If the server has already been started.
	 */
	protected void startDispatch() throws ServerException {
		if (started)
			throw new ServerException("Server already started");

		started = true;

		Thread dispatcher = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					launchNewThread();
				} catch (Exception e) {
					Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				}
			}
		}, "JNetwork-Server-Accept-Dispatcher");
		dispatcher.start();
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
	 * removed by <b><code>removeClient(SocketPackage client)</code></b>
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
	 * client is removed by <b>
	 * <code>removeClient(SocketPackage event)</code></b>
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
	 * Sets data to the SocketPackage of a connected client.
	 * 
	 * @param client
	 *            - The SocketPackage of the client whose data will be set to.
	 * @param data
	 *            - The data.
	 * 
	 * @return boolean - If the client was actually connected.
	 */
	public boolean setConnectionData(SocketPackage client, Object... data) {
		refresh();
		if (clients.contains(client)) {
			client.setExtraData(data);

			clients.set(clients.indexOf(client), client);
		} else {
			return false;
		}
		return true;
	}

	/**
	 * Removes the client from the server's list of clients and closes it if it
	 * was not already closed.
	 * 
	 * @param client
	 *            - The SocketPackage of the client.
	 * 
	 * @returns boolean - If the client was actually in the server's
	 *          {@code List <SocketPackage> }
	 * @throws IOException
	 *             If there is an error closing the client if it is not already
	 *             closed.
	 */
	public void removeClient(SocketPackage client) throws IOException {
		if (clients.contains(client)) {
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
	 *          {@code List <SocketPackage> }
	 * 
	 * @throws IOException
	 *             If there is an error closing the client if it is not already
	 *             closed.
	 */
	public void removeClient(SocketAddress addr) throws IOException {
		removeClient(getClient(addr));
	}

	/**
	 * Gets a connected client's <code>SocketPackage</code> by their IP address.
	 * 
	 * @param addr
	 *            - The <code>SocketAddress</code> of the client.
	 * 
	 * @return SocketPackage - If a user has the same <code>SocketAddress</code>
	 *         as the <code>addr</code> parameter.<br>
	 *         null - If no <code>SocketPackage</code> is found with the same
	 *         <code>SocketAddress</code> as the <code>addr</code> parameter.
	 */
	public SocketPackage getClient(SocketAddress addr) {
		for (SocketPackage evt : clients) {
			if (evt.getConnection().getRemoteSocketAddress().toString().equals(addr.toString()))
				return evt;
		}
		return null;
	}

	/**
	 * Gets a connected client's <code>SocketPackage</code> by their
	 * <code>Socket</code>.
	 * 
	 * @param socket
	 *            - The <code>Socket</code> of the client.
	 * @return SocketPackage - If a user has the same <code>Socket</code> as the
	 *         <code>socket</code> parameter. <br>
	 *         null - If no <code>SocketPackage</code> is found with the same
	 *         <code>Socket</code> as the <code>socket</code> parameter.
	 * 
	 * @see org.jnetwork.SocketPackage
	 */
	public SocketPackage getClient(Socket socket) {
		for (SocketPackage evt : clients) {
			if (evt.getConnection().equals(socket))
				return evt;
		}
		return null;
	}

	/**
	 * Gets the SocketPackage for every client with matching extra data as the
	 * <code>data</code> parameter.
	 * 
	 * @param data
	 *            - The data to check for.
	 * 
	 * @return SocketPackage[] - An array containing each
	 *         <code>SocketPackage</code> with matching data.<br>
	 *         null - If no <code>SocketPackage</code> has the same data.
	 */
	public SocketPackage[] getClientsByData(Object... data) {
		ArrayList<SocketPackage> evts = new ArrayList<SocketPackage>();

		for (int i = 0; i < clients.size(); i++) {
			if (Arrays.equals(clients.get(i).getExtraData(), data)) {
				evts.add(clients.get(i));
			}
		}

		return evts.toArray(new SocketPackage[evts.size()]);
	}

	/**
	 * Gets the SocketPackage for every client with matching extra data at the
	 * same index as the <code>index</code> and data at that index equal to the
	 * <code>data</code> parameter.<br>
	 * <br>
	 * For example, assume that a client has extra data like so: <br>
	 * <code>{1, "username"}</code><br>
	 * and another client has extra data like so:<br>
	 * <code>{2, "username"}</code>. If <code>getByDataIndex(0, 1)</code><br>
	 * is called, this will return the SocketPackage for the first client. If
	 * <code>getByDataIndex(1, "username")</code> is called, this will return
	 * the SocketPackage for both clients.<br>
	 * <br>
	 * If an <code>ArrayIndexOutOfBoundsException</code> is thrown, it will be
	 * ignored and the method will continue on as normal.
	 * 
	 * @param index
	 *            - The index to check the data at.
	 * @param data
	 *            - The data to check for at the index.
	 * 
	 * @return SocketPackage[] - An array containing each
	 *         <code>SocketPackage</code> with matching data at the index.<br>
	 *         null - If no <code>SocketPackage</code> has the same data.
	 */
	public SocketPackage[] getClientsByDataIndex(int index, Object data) {
		ArrayList<SocketPackage> evts = new ArrayList<SocketPackage>();

		for (int i = 0; i < clients.size(); i++) {
			try {
				data = clients.get(i).getExtraData()[index].getClass();
				if (Objects.equals(clients.get(i).getExtraData()[index], data)) {
					evts.add(clients.get(i));
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				continue;
			}
		}

		return evts.toArray(new SocketPackage[evts.size()]);
	}

	/**
	 * Removes all of the closed clients that are still in the server's internal
	 * {@code List<SocketPackage>}.
	 **/
	public void refresh() {
		ArrayList<SocketPackage> closedClients = new ArrayList<>();

		for (SocketPackage ce : clients) {
			Connection client = ce.getConnection();
			if (client.isClosed()) {
				closedClients.add(ce);
			}
		}

		// prevent CME
		for (SocketPackage closedClient : closedClients) {
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
	public SocketPackage[] getClients() {
		return clients.toArray(new SocketPackage[clients.size()]);
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
}