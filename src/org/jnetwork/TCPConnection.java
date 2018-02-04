package org.jnetwork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.Objects;

/**
 * A TCP representation of the Connection object. Used for writing and reading
 * data with a TCPServer.
 * 
 * @author Lucas Baizer
 */
public class TCPConnection extends Connection {
	protected Socket connection;
	protected OutputStream out;
	protected InputStream in;
	protected InetSocketAddress address;

	protected TCPConnection() {
		super();
	}

	/**
	 * @return The socket that the <code>Connection</code> is built off of.
	 */
	public Socket getConnection() {
		return connection;
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
	public TCPConnection(Socket socket) throws IOException {
		super(socket.getInetAddress().getHostAddress(), socket.getPort());

		this.connection = socket;
		this.out = socket.getOutputStream();
		this.in = socket.getInputStream();
	}

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
	public TCPConnection(String host, int port) throws UnknownHostException, IOException {
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
	public TCPConnection(String host, int port, boolean connectNow) throws UnknownHostException, IOException {
		super(host, port);

		if (Objects.equals(host, null) || Objects.equals(port, null))
			throw new NullPointerException();

		if (connectNow) {
			this.connection = new Socket(host, port);
			this.out = connection.getOutputStream();
			this.in = connection.getInputStream();
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
	public void connect() throws UnknownHostException, IOException {
		this.connection = new Socket(address.getHostString(), address.getPort());
		this.out = connection.getOutputStream();
		this.in = connection.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() {
		return out;
	}

	@Override
	public InputStream getInputStream() {
		return in;
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		return connection.getRemoteSocketAddress();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		return connection.getLocalSocketAddress();
	}

	@Override
	public boolean isClosed() {
		return connection.isClosed();
	}

	@Override
	public void close() throws IOException {
		connection.close();

		super.close();
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		out.write(bytes, off, len);
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public int read(byte[] arr, int off, int len) throws IOException {
		return in.read(arr, off, len);
	}

	@Override
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
	}
}
