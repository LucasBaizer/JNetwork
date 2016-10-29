package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

/**
 * A UDP representation of the Connection object. Used for writing and reading
 * data with a UDPServer.
 * 
 * @author Lucas Baizer
 */
public class UDPConnection extends Connection {
	protected InetSocketAddress targetAddress;
	protected DatagramSocket socket;
	protected int bufferSize = 1024;

	public UDPConnection(String host, int port) throws SocketException {
		super(host, port);

		socket = new DatagramSocket();
		targetAddress = new InetSocketAddress(host, port);
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

	@Override
	public void writeObject(Serializable obj) throws IOException {
		byte[] bytes = UDPUtils.serializeObject(obj);
		write(bytes, 0, bytes.length);
	}

	@Override
	public void writeUnshared(Serializable obj) throws IOException {
		writeObject(obj);
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
	public void read(byte[] arr) throws IOException {
		read(arr, 0, arr.length);
	}

	@Override
	public void read(byte[] arr, int off, int len) throws IOException {
		DatagramPacket packet = new DatagramPacket(arr, off, len);
		socket.receive(packet);

		arr = packet.getData();
	}

	@Override
	public Serializable readObject() throws IOException, ClassNotFoundException {
		return UDPUtils.deserializeObject(readPacket().getData());
	}

	@Override
	public Serializable readUnshared() throws IOException, ClassNotFoundException {
		return readObject();
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
}
