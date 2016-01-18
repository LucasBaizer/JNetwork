package org.jnetwork;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * (Functionally) a struct used to store all the data created when a client
 * connects to a server.
 * 
 * @author Lucas Baizer
 */
public class SocketPackage {
	private Socket socket;
	private AdvancedInputStream in;
	private AdvancedOutputStream out;
	private Object[] data;

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
	public SocketPackage(Socket socket, AdvancedInputStream in, AdvancedOutputStream out, Server server,
			Object... extraData) {
		this.socket = socket;
		this.in = in;
		this.out = out;
		this.data = extraData;
	}

	/**
	 * Gets the <code>Socket</code> initialized at construction.
	 * 
	 * @return The <code>Socket</code>.
	 */
	public Socket getSocket() {
		return this.socket;
	}

	/**
	 * @return the <code>ObjectInputStream</code>.
	 */
	public AdvancedInputStream getInputStream() {
		return this.in;
	}

	/**
	 * @return the <code>AdvancedOutputStream</code>.
	 */
	public AdvancedOutputStream getOutputStream() {
		return this.out;
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
		return getSocket().getRemoteSocketAddress().toString();
	}
}