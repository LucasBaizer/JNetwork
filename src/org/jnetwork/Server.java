package org.jnetwork;

import java.io.Closeable;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import org.jnetwork.listener.ClientConnectionListener;
import org.jnetwork.listener.ClientDisconnectionListener;

/**
 * A server-side utility used for easily handling an infinite amount of
 * connections between clients. When the constructor is called, a
 * <code>ServerSocket</code> is bound to a given port and then created. A new
 * thread is then launched, which will wait for a client to connect, and when
 * they do, a new <code>SocketPackage</code> will be created containing all the
 * information required for the connection with the client. In another new
 * thread, launched from the new thread initially launched, the
 * <code>ClientConnectionListener.clientConnected</code> will be called and
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
 * <code>ClientDisconnectionListener</code> added with
 * {@link Server#addClientDisconnectionListener(ClientDisconnectionListener)}
 * will be called and parameterized with the client's <code>SocketPackage</code>
 * . <br>
 * <br>
 * 
 * @see java.net.ServerSocket
 * @see org.jnetwork.SocketPackage
 * @see org.jnetwork.listener.network.event.RefreshListener
 * @see org.jnetwork.listener.network.event.ClientConnectionListener
 * @see org.jnetwork.listener.ClientDisconnectionListener.event.
 *      ClientRemovedListener
 * @see org.jnetwork.SocketPackage
 * 
 * @author Lucas Baizer
 */
public class Server implements Closeable {
	private ServerSocket server;
	private ClientConnectionListener thread;
	private int port;
	private ArrayList<SocketPackage> clients = new ArrayList<SocketPackage>();
	private ArrayList<SavedData> savedData = new ArrayList<SavedData>();
	private ArrayList<ClientDisconnectionListener> removers = new ArrayList<ClientDisconnectionListener>();
	private int maxClients = Integer.MAX_VALUE;
	private boolean started;
	private Object closeWaiter = new Object();

	/**
	 * Constructs a new <code>Server</code> and starts a new
	 * <code>ServerSocket</code> on port <code>port</code>.
	 * 
	 * @param port
	 *            - The port to start the server on.
	 * @param clientSocketThread
	 *            - The <code>ClientConnectionListener<code> to be called when a
	 *            client connects. It will be given the client's <code>Socket</code>
	 *            and an instance of <code>this</code>.
	 * 
	 * @throws NullPointerException
	 *             If <code>clientSocketThread</code> is null.
	 * @throws IOException
	 *             If there is an error starting the <code>ServerSocket</code>.
	 */
	public Server(int port, ClientConnectionListener clientSocketThread) throws IOException {
		if (clientSocketThread == null)
			throw new NullPointerException("ClientConnectionListener is null");

		this.server = new ServerSocket(port);
		this.thread = clientSocketThread;
		this.port = port;
	}

	public void start() throws ServerException {
		if (started)
			throw new ServerException("Server already started");

		started ^= true;

		Thread dispatcher = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					launchNewThread();
				} catch (Exception e) {
					Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				}
			}
		});
		dispatcher.setName("JNetwork-Server-Accept-Dispatcher");
		dispatcher.start();
	}

	/**
	 * Calls {@link #Server(int, ClientConnectionListener)} and sets the maximum
	 * clients to the <code>maxClients</code> parameter.
	 * 
	 * @param maxClients
	 *            - The amount of clients that can be connected to the server at
	 *            any given point in time.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if <code>maxClients</code> is less than one.
	 */
	public Server(int port, int maxClients, ClientConnectionListener clientSocketThread) throws IOException {
		this(port, clientSocketThread);

		if (maxClients < 1)
			throw new IndexOutOfBoundsException(Integer.toString(maxClients));
		this.maxClients = maxClients;
	}

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
	public void addClientDisconnectionListener(ClientDisconnectionListener listener) {
		removers.add(listener);
	}

	/**
	 * Removes a <code>ClientDisconnectionListener</code> to be called on when a
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
	public boolean removeClientDisconnectionListener(ClientDisconnectionListener listener) {
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
	 * Gets every <code>ClientDisconnectionListener</code> added with <b>
	 * <code>addClientDisconnectionListener(ClientDisconnectionListener)</code>
	 * </b>.
	 * 
	 * @return ClientDisconnectionListener[] - The array containing the
	 *         ClientRemovedListeners.
	 */
	public ClientDisconnectionListener[] getClientDisconnectionListeners() {
		return removers.toArray(new ClientDisconnectionListener[removers.size()]);
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
	 * Saves the data of a connected client to a List. If the client
	 * disconnects, and than later reconnects, and their connection data was
	 * saved before they disconnected, their connection data will be set to the
	 * previously saved data.
	 * 
	 * @param client
	 *            - The <code>SocketPackage</code> whose data will be saved.
	 * @return boolean - If <code>client</code> was actually connected.
	 * 
	 * @throws NullPointerException
	 *             If <code>client</code> is <code>null</code>.
	 */
	public boolean saveConnectionData(SocketPackage client) {
		if (client == null)
			throw new NullPointerException();

		if (clients.contains(client)) {
			if (client.getExtraData().length != 0) {
				savedData.add(new SavedData(client, client.getExtraData()));
			}
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

			for (ClientDisconnectionListener listener : removers) {
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
		ArrayList<Connection> closedClients = new ArrayList<>();

		for (SocketPackage ce : clients) {
			Connection client = ce.getConnection();
			if (client.isClosed()) {
				closedClients.add(client);
			}
		}

		// prevent CME
		for (Connection closedClient : closedClients) {
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

	/**
	 * Sets the maximum amount of clients allowed to be connected to the server
	 * at a point in time. Defaults to <code>Integer.MAX_VALUE</code>.
	 * 
	 * @param clients
	 *            - The new cap.
	 * 
	 * @see java.lang.Integer#MAX_VALUE
	 */
	public void setMaxClients(int clients) {
		maxClients = clients;
	}

	/**
	 * Gets the maximum amount of clients allowed to be connected to the server
	 * at a point in time. Defaults to <code>Integer.MAX_VALUE</code>.
	 * 
	 * @return int - The cap.
	 * 
	 * @see java.lang.Integer#MAX_VALUE
	 */
	public int getMaxClients() {
		return maxClients;
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
		server.close();

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
	private void launchNewThread() throws IOException, InterruptedException {
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
		while (clients.size() == maxClients) {
			Thread.sleep(20);
		}
		final SocketPackage event = new SocketPackage(new Connection(client));

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
				thread.clientConnected(event);
				try {
					removeClient(event);
				} catch (IOException e) {
					Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				}
			}
		});
		event.setHoldingThread(thr);
		thr.setName("JNetwork-Server-Thread-" + client.getRemoteSocketAddress());
		thr.start();

		launchNewThread();
	}

	/**
	 * Essentially a <code>struct</code> to hold saved data saved with
	 * {@link Server#saveConnectionData(SocketPackage)}.
	 * 
	 * @author Lucas Baizer
	 */
	private static class SavedData {
		private SocketPackage pkg;
		private Object[] data;

		public SavedData(SocketPackage pkg, Object[] data) {
			this.pkg = pkg;
			this.data = data;
		}
	}
}