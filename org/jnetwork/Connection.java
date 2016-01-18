package org.jnetwork;

import java.io.Closeable;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * A client-side class used for sending and reading objects easily from a
 * server. It is not recommended to use this class for any serverside code. It
 * is designed for client-side only.
 * 
 * @see java.net.Socket
 * 
 * @author Lucas Baizer
 */
public class Connection implements Closeable {
	private Socket client;
	private AdvancedOutputStream out;
	private AdvancedInputStream in;
	private InetSocketAddress address;

	/**
	 * Constructs a new
	 * <code>Socket<code> off of the given parameters. Equivalent to calling <b><code>new Connection(host, port, true)</code>
	 * </b>.
	 * 
	 * @param host
	 *            - The host address (ex. "1.2.3.4").
	 * @param port
	 *            - The host port (ex. 80).
	 * @throws UnknownHostException
	 *             If the host does not exist.
	 * @throws IOException
	 *             If a general error occurs during initialization of the
	 *             <code>Socket</code> and the streams.
	 * @throws NullPointerException
	 *             If <code>host</code> or </code>port</code> is
	 *             <code>null</code>.
	 */
	public Connection(String host, int port) throws UnknownHostException, IOException {
		this(host, port, true);
	}

	/**
	 * Constructs a new <code>Socket</code> off of the <code>host</code> and
	 * <code>port</code> parameters, but will only instantiate the
	 * <code>Socket</code> if <code>connectNow</code> is <code>true</code>.
	 * Otherwise, the host and port will be saved in variables, and you will
	 * have to instantiate the <code>Socket</code> later by calling
	 * <b>connect()</b>.
	 * 
	 * @param host
	 *            - The host address (ex. "1.2.3.4").
	 * @param port
	 *            - The host port (ex. 80).
	 * 
	 * @param connectNow
	 *            - If the <code>Socket</code> should be instantiated at the
	 *            time of calling this constructor.
	 * @throws UnknownHostException
	 *             If the host does not exist.
	 * @throws IOException
	 *             If a general error occurs during initialization of the
	 *             <code>Socket</code> and the streams.
	 * @throws NullPointerException
	 *             If <code>host</code> or <code>port</code> is
	 *             <code>null</code>.
	 */
	public Connection(String host, int port, boolean connectNow) throws UnknownHostException, IOException {
		if (Objects.equals(host, null) || Objects.equals(port, null))
			throw new NullPointerException();

		if (connectNow) {
			this.client = new Socket(host, port);
			this.out = new AdvancedOutputStream(client.getOutputStream());
			this.in = new AdvancedInputStream(client.getInputStream());
		} else {
			this.address = new InetSocketAddress(host, port);
		}
	}

	/**
	 * Used for instantiating a <code>Socket</code> after calling <b>
	 * <code>Connection(host, port, false)</code></b>.
	 * 
	 * @return An instance of <b><code>this</code><b>.
	 * 
	 * @throws UnknownHostException
	 *             If the host does not exist.
	 * @throws IOException
	 *             If a general error occurs during initialization of the
	 *             <code>Socket</code> and the streams.
	 */
	public Connection connect() throws UnknownHostException, IOException {
		this.client = new Socket(address.getHostString(), address.getPort());
		this.out = new AdvancedOutputStream(client.getOutputStream());
		this.in = new AdvancedInputStream(client.getInputStream());

		return this;
	}

	/**
	 * @return The client's output stream.
	 */
	public ObjectOutputStream getOutputStream() {
		return out;
	}

	/**
	 * @return The client's input stream.
	 */
	public AdvancedInputStream getInputStream() {
		return in;
	}

	/**
	 * Gets the remote socket address of the <code>Socket</code>.
	 * 
	 * @return The <code>SocketAddress</code>.
	 */
	public SocketAddress getRemoteSocketAddress() {
		return client.getRemoteSocketAddress();
	}

	/**
	 * Gets the local socket address of the <code>Socket</code>.
	 * 
	 * @return The <code>SocketAddress</code>.
	 */
	public SocketAddress getLocalSocketAddress() {
		return client.getLocalSocketAddress();
	}

	/**
	 * Gets if the <code>Socket</code> is closed.
	 * 
	 * @return If the <code>Socket</code> is closed.
	 */
	public boolean isClosed() {
		return client.isClosed();
	}

	/**
	 * @return The socket that the <code>Connection</code> uses for internal
	 *         connections.
	 */
	public Socket getSocket() {
		return client;
	}

	/**
	 * Closes the <code>Socket</code>.
	 * 
	 * @throws IOException
	 *             If there is an error closing the <code>Socket</code>.
	 * 
	 * @see java.io.Closeable
	 * @see java.net.Socket
	 */
	@Override
	public void close() throws IOException {
		client.close();
	}

	/**
	 * Returns the <code>Socket</code>'s remote socket address.
	 */
	@Override
	public String toString() {
		return client.getRemoteSocketAddress().toString();
	}
}