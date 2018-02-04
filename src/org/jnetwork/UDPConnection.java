package org.jnetwork;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 * A UDP representation of the Connection object. Used for writing and reading
 * data with a UDPServer.
 * 
 * @author Lucas Baizer
 */
public class UDPConnection extends Connection {
	protected InputStream in;
	protected OutputStream out;
	protected InetSocketAddress targetAddress;
	protected DatagramSocket socket;
	protected int bufferSize = 1024;

	public UDPConnection(String host, int port) throws IOException {
		super(host, port);

		socket = new DatagramSocket();
		targetAddress = new InetSocketAddress(host, port);
		setupStreams();
	}

	/**
	 * Constructs a new UDPConnection off of an existing DatagramSocket.
	 * 
	 * @param socket
	 *            The DatagramSocket.
	 */
	public UDPConnection(DatagramSocket socket) {
		super(socket.getInetAddress() != null ? socket.getInetAddress().getHostAddress() : null, socket.getPort());

		this.socket = socket;
		setupStreams();
	}

	private void setupStreams() {
		in = new InputStream() {
			@Override
			public int read() throws IOException {
				return UDPConnection.this.read();
			}

			@Override
			public int read(byte[] b) throws IOException {
				return UDPConnection.this.read(b);
			}

			@Override
			public int read(byte[] b, int off, int len) throws IOException {
				return UDPConnection.this.read(b, off, len);
			}
		};
		out = new OutputStream() {
			@Override
			public void write(int b) throws IOException {
				UDPConnection.this.write(b);
			}

			@Override
			public void write(byte[] b) throws IOException {
				UDPConnection.this.write(b);
			}

			@Override
			public void write(byte[] b, int off, int len) throws IOException {
				UDPConnection.this.write(b, off, len);
			}
		};
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		return socket.getRemoteSocketAddress();
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		return socket.getLocalSocketAddress();
	}

	@Override
	public boolean isClosed() {
		return socket.isClosed();
	}

	@Override
	public void write(int b) throws IOException {
		write(new byte[] { (byte) b });
	}

	@Override
	public void write(byte[] bytes) throws IOException {
		write(bytes, 0, bytes.length);
	}

	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException {
		socket.send(new DatagramPacket(bytes, offset, length, targetAddress));
	}

	protected DatagramPacket readPacket() throws IOException {
		byte[] receive = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(receive, receive.length);
		socket.receive(packet);

		return packet;
	}

	@Override
	public int read() throws IOException {
		return readPacket().getData()[0];
	}

	@Override
	public int read(byte[] arr) throws IOException {
		return read(arr, 0, arr.length);
	}

	@Override
	public int read(byte[] arr, int off, int len) throws IOException {
		DatagramPacket packet = new DatagramPacket(arr, off, len);
		socket.receive(packet);

		System.arraycopy(packet.getData(), 0, arr, off, len);
		return packet.getLength();
	}

	/**
	 * @return the default buffer size of data that is send and received.
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * Sets the default buffer size of data that is send and received.
	 */
	public void setBufferSize(int bufferSize) {
		this.bufferSize = bufferSize;
	}

	/**
	 * @return the InetSocketAddress of the device that data will be sent to
	 *         when and of the <code>writeXXX</code> methods are called.
	 */
	public InetSocketAddress getTargetAddress() {
		return targetAddress;
	}

	/**
	 * Sets the InetSocketAddress of the device that data will be sent to when
	 * and of the <code>writeXXX</code> methods are called.
	 */
	public void setTargetAddress(InetSocketAddress targetAddress) {
		this.targetAddress = targetAddress;
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
	public void setOutputStream(OutputStream out) {
		this.out = out;
	}

	@Override
	public void setInputStream(InputStream in) {
		this.in = in;
	}
}
