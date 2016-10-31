package org.jnetwork;

/**
 * (Functionally) a struct used to store all the data created when a client
 * connects to a server.
 * 
 * @author Lucas Baizer
 */
public class SocketPackage {
	private Connection socket;
	private Thread holder;

	/**
	 * Creates a new SocketPackage with all the required data.
	 * 
	 * @param socket
	 *            - The socket.
	 */
	public SocketPackage(Connection socket) {
		this.socket = socket;
	}

	/**
	 * Creates a new SocketPackage with all the required data and a holding
	 * thread.
	 * 
	 * @param socket
	 *            - The socket.
	 * @param holder
	 *            - The thread which this SocketPackage is connected to.
	 */
	SocketPackage(Connection socket, Thread holder) {
		this(socket);

		this.holder = holder;
	}

	/**
	 * Gets the <code>Connection</code> initialized at construction.
	 * 
	 * @return The <code>Connection</code>.
	 */
	public Connection getConnection() {
		return this.socket;
	}

	/**
	 * Gets a String representation of the object in the form of the socket's
	 * remote address.
	 */
	@Override
	public String toString() {
		return getConnection().getRemoteSocketAddress().toString();
	}

	/**
	 * @return the thread that this SocketPackage is connected to.
	 */
	Thread getHoldingThread() {
		return holder;
	}

	/**
	 * Sets the thread that this SocketPackage is connected to.
	 * 
	 * @param holder
	 *            - The thread.
	 */
	void setHoldingThread(Thread holder) {
		this.holder = holder;
	}
}