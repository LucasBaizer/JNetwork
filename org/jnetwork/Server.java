package org.jnetwork;

import java.io.Closeable;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.Arrays;

import org.jnetwork.event.ClientConnectionListener;
import org.jnetwork.event.ClientRemovedListener;
import org.jnetwork.event.RefreshListener;

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
 * <code>ClientRemovedListener</code> added with
 * {@link Server#addClientRemovedListener(ClientRemovedListener)} will be called
 * and parameterized with the client's <code>SocketPackage</code>. <br>
 * <br>
 * 
 * @see java.net.ServerSocket
 * @see org.jnetwork.SocketPackage
 * @see javax.network.event.RefreshListener
 * @see javax.network.event.ClientConnectionListener
 * @see javax.network.event.ClientRemovedListener
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
	private ArrayList<RefreshListener> refreshers = new ArrayList<RefreshListener>();
	private ArrayList<ClientRemovedListener> removers = new ArrayList<ClientRemovedListener>();
	private int maxClients = Integer.MAX_VALUE;

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

		launchNewThread();
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
	 * Adds a <code>RefreshListener</code> to be called on when a client is
	 * removed by <b><code>refresh()</code></b>
	 * 
	 * @param listener
	 *            - The listener to be added.
	 * 
	 * @throws NullPointerException
	 *             If <code>listener</code> is null.
	 */
	public void addRefreshListener(RefreshListener listener) {
		if (listener == null)
			throw new NullPointerException();

		refreshers.add(listener);
	}

	/**
	 * Removes a <code>RefreshListener</code> to be called on when a client is
	 * removed by <b><code>refresh()</code></b>
	 * 
	 * @param listener
	 *            - The listener to be removed.
	 * 
	 * @return boolean - If the listener was actually added in the first place.
	 * 
	 * @throws NullPointerException
	 *             If <code>listener</code> is null.
	 */
	public boolean removeRefreshListener(RefreshListener listener) {
		if (listener == null)
			throw new NullPointerException();

		return refreshers.remove(listener);
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
	public void addClientRemovedListener(ClientRemovedListener listener) {
		removers.add(listener);
	}

	/**
	 * Removes a <code>ClientRemovedListener</code> to be called on when a
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
	public boolean removeClientRemovedListener(ClientRemovedListener listener) {
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
			addresses[i] = clients.get(i).getSocket().getRemoteSocketAddress();
		}

		return addresses;
	}

	/**
	 * Gets every <code>RefreshListener</code> added with <b>
	 * <code>addRefreshListener(RefreshListener listener)</code></b>.
	 * 
	 * @return RefreshListener[] - The array containing the RefreshListeners.
	 */
	public RefreshListener[] getRefreshListeners() {
		RefreshListener[] listeners = new RefreshListener[refreshers.size()];

		for (int i = 0; i < refreshers.size() - 1; i++) {
			listeners[i] = refreshers.get(i);
		}

		return listeners;
	}

	/**
	 * Gets every <code>ClientRemovedListener</code> added with <b>
	 * <code>addClientRemovedListener(ClientRemovedListener)</code></b>.
	 * 
	 * @return ClientRemovedListener[] - The array containing the
	 *         ClientRemovedListeners.
	 */
	public ClientRemovedListener[] getSocketRemovedListeners() {
		ClientRemovedListener[] listeners = new ClientRemovedListener[refreshers.size()];

		for (int i = 0; i < removers.size() - 1; i++) {
			listeners[i] = removers.get(i);
		}

		return listeners;
	}

	/**
	 * Launches a new thread so a new client can connect.
	 */
	private void launchNewThread() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// wait until a client disconnects if the maximum amount of
					// clients is full
					while (clients.size() == maxClients) {
						Thread.sleep(20);
					}
					Socket client = Server.this.server.accept();

					AdvancedInputStream in = new AdvancedInputStream(client.getInputStream());
					AdvancedOutputStream out = new AdvancedOutputStream(client.getOutputStream());

					final SocketPackage event = new SocketPackage(client, in, out, Server.this);

					refresh();
					clients.add(event);
					refresh();

					// sets saved data
					for (SavedData data : savedData)
						if (event == data.pkg)
							event.setExtraData(data.data);
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								thread.clientConnected(event);

								// remove client after thread terminates
								if (clients.contains(event))
									removeClient(event);
							} catch (IOException e) {
								Thread.currentThread().getUncaughtExceptionHandler()
										.uncaughtException(Thread.currentThread(), e);
							}
						}
					}).start();

					launchNewThread();
				} catch (Exception ioe) {
					Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), ioe);
				}
			}
		}).start();
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
	 *            - The ConnectionEvent.
	 * 
	 * @returns boolean - If the client was actually in the server's
	 *          {@code List <SocketPackage> }
	 * @throws IOException
	 *             If there is an error closing the client if it is not already
	 *             closed.
	 */
	public boolean removeClient(SocketPackage client) throws IOException {
		if (clients.contains(client)) {
			clients.remove(client);

			if (!client.getSocket().isClosed())
				client.getSocket().close();
			for (ClientRemovedListener listener : removers) {
				listener.clientRemoved(client);
			}
		} else {
			return false;
		}
		refresh();
		return true;
	}

	/**
	 * Removes the client from the server's list of clients and closes it if it
	 * was not already closed.
	 * 
	 * @param client
	 *            - The client's address.
	 * 
	 * @returns boolean - If the client was actually in the server's
	 *          {@code List <SocketPackage> }
	 * 
	 * @throws IOException
	 *             If there is an error closing the client if it is not already
	 *             closed.
	 */
	public boolean removeClient(SocketAddress client) throws IOException {
		boolean legit = false;
		refresh();
		for (SocketPackage evt : clients) {
			if (evt.getSocket().getRemoteSocketAddress().toString().equals(client.toString())) {
				legit = true;
				clients.remove(client);

				if (!evt.getSocket().isClosed())
					evt.getSocket().close();
				evt = new SocketPackage(evt.getSocket(), evt.getInputStream(), evt.getOutputStream(), this);
				for (ClientRemovedListener listener : removers) {
					listener.clientRemoved(evt);
				}
			}
		}
		refresh();
		return legit;
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
			if (evt.getSocket().getRemoteSocketAddress().toString().equals(addr.toString()))
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
			if (evt.getSocket().equals(socket))
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
		SocketPackage[] pkg = new SocketPackage[evts.size()];
		int i = 0;
		for (SocketPackage pack : evts) {
			if (pack != null)
				pkg[i++] = pack;
		}
		return pkg;
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
				data = clients.get(i).getExtraData()[index].getClass().cast(data);
				if (clients.get(i).getExtraData()[index].equals(data) || clients.get(i).getExtraData()[index] == data) {
					evts.add(clients.get(i));
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				continue;
			}
		}
		SocketPackage[] pkg = new SocketPackage[evts.size()];
		int i = 0;
		for (SocketPackage pack : evts) {
			if (pack != null)
				pkg[i++] = pack;
		}
		return pkg;
	}

	/**
	 * Removes all of the closed clients that are still in the server's internal
	 * {@code List<SocketPackage>}.
	 **/
	public void refresh() {
		ArrayList<Socket> closedClients = new ArrayList<Socket>();

		for (SocketPackage ce : clients) {
			Socket client = ce.getSocket();
			if (client.isClosed()) {
				for (RefreshListener listener : refreshers) {
					listener.clientDisconnect(ce);
				}
				closedClients.add(client);
			}
		}

		for (Socket closedClient : closedClients) {
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
		refresh();
		SocketPackage[] clientArray = new SocketPackage[clients.size()];

		for (int i = 0; i < clients.size(); i++) {
			clientArray[i] = clients.get(i);
		}

		return clientArray;
	}

	/**
	 * Gets the <code>SocketPackage</code> of every connected client.
	 * 
	 * @return SocketPackage[] - The array containing the
	 *         <code>SocketPackage</code> of every connected client.
	 * 
	 * @see org.jnetwork.SocketPackage
	 */
	public SocketPackage[] getConnectionEvents() {
		refresh();
		SocketPackage[] events = new SocketPackage[clients.size()];
		for (int i = 0; i < clients.size(); i++) {
			events[i] = clients.get(i);
		}

		return events;
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
	 * Closes the <code>ServerSocket</code>.
	 * 
	 * @see java.io.Closeable#close()
	 */
	@Override
	public void close() throws IOException {
		server.close();
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