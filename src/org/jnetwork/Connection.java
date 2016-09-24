package org.jnetwork;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

import javax.net.ssl.SSLSocketFactory;

/**
 * A class used for writing objects out to and reading objects from a server.
 * 
 * @see java.net.Socket
 * 
 * @author Lucas Baizer
 */
public class Connection implements Closeable {
	private Socket connection;
	private AdvancedOutputStream out;
	private AdvancedInputStream in;
	private InetSocketAddress address;
	private Object closeWaiter = new Object();

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
			this.connection = SSLSocketFactory.getDefault().createSocket(host, port);
			this.out = new AdvancedOutputStream(connection.getOutputStream());
			this.in = new AdvancedInputStream(connection.getInputStream());
		} else {
			this.address = new InetSocketAddress(host, port);
		}
	}

	/**
	 * Constructs a new Connection object, with its internal socket as the
	 * <code>socket</code> parameter. A new AdvancedInputStream and
	 * AdvancedOutputStream get instantiated, so <b>do not have any
	 * instantiated</b>!
	 * 
	 * @param socket
	 *            - The internal socket that this instantiation of Connection
	 *            will be built off of.
	 * @throws IOException
	 *             If an error occurs during instantiating the streams.
	 * @throws NullPointerException
	 *             If the <code>socket</code> parameter is null.
	 */
	public Connection(Socket socket) throws IOException {
		if (socket == null)
			throw new NullPointerException();

		this.connection = socket;
		this.out = new AdvancedOutputStream(socket.getOutputStream());
		this.in = new AdvancedInputStream(socket.getInputStream());
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
		this.connection = new Socket(address.getHostString(), address.getPort());
		this.out = new AdvancedOutputStream(connection.getOutputStream());
		this.in = new AdvancedInputStream(connection.getInputStream());

		return this;
	}

	/**
	 * @return The connection's output stream.
	 */
	public AdvancedOutputStream getOutputStream() {
		return out;
	}

	/**
	 * @return The connection's input stream.
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
		return connection.getRemoteSocketAddress();
	}

	/**
	 * Gets the local socket address of the <code>Socket</code>.
	 * 
	 * @return The <code>SocketAddress</code>.
	 */
	public SocketAddress getLocalSocketAddress() {
		return connection.getLocalSocketAddress();
	}

	/**
	 * Gets if the <code>Socket</code> is closed.
	 * 
	 * @return If the <code>Socket</code> is closed.
	 */
	public boolean isClosed() {
		return connection.isClosed();
	}

	/**
	 * @return The socket that the <code>Connection</code> is built off of.
	 */
	public Socket getConnection() {
		return connection;
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
		connection.close();

		synchronized (closeWaiter) {
			closeWaiter.notifyAll();
		}
	}

	/**
	 * Returns the <code>Socket</code>'s remote socket address.
	 */
	@Override
	public String toString() {
		return connection.getRemoteSocketAddress().toString();
	}

	/**
	 * Causes the current thread to block until the connection is closed.
	 * 
	 * @throws InterruptedException
	 *             Specified by {@link Object#wait()}.
	 * 
	 * @see #getConnection()
	 */
	public void waitUntilClose() throws InterruptedException {
		synchronized (closeWaiter) {
			closeWaiter.wait();
		}
	}
}