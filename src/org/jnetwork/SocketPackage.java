package org.jnetwork;

/**
 * (Functionally) a struct used to store all the data created when a client
 * connects to a server.
 * 
 * @author Lucas Baizer
 */
public class SocketPackage {
	private Connection socket;
	private Object[] data;
	private Thread holder;

	/**
	 * Creates a new SocketPackage with all the required data.
	 * 
	 * @param socket
	 *            - The socket.
	 * @param in
	 *            - The input stream.
	 * @param out
	 *            - The output stream.
	 * @param extraData
	 *            - Any extra data to add to the package.
	 */
	public SocketPackage(Connection socket, Object... extraData) {
		this.socket = socket;
		this.data = extraData;
	}

	/**
	 * Creates a new SocketPackage with all the required data and a holding
	 * thread.
	 * 
	 * @param socket
	 *            - The socket.
	 * @param holder
	 *            - The thread which this SocketPackage is connected to.
	 * @param extraData
	 *            - Any extra data to add to the package.
	 */
	SocketPackage(Connection socket, Thread holder, Object... extraData) {
		this(socket, extraData);

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
	 * Gets the extra data set when the constructor was called.
	 * 
	 * @return The data.
	 */
	public Object[] getExtraData() {
		return data;
	}

	/**
	 * Sets the extra data.
	 */
	public void setExtraData(Object... extraData) {
		this.data = extraData;
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