package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.SocketException;

public class UDPConnection extends Connection {
	private InetSocketAddress targetAddress;
	private DatagramSocket socket;
	private int bufferSize = 1024;

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
		socket.send(new DatagramPacket(bytes, 0, bytes.length, targetAddress));
	}

	@Override
	public void write(byte[] bytes, int offset, int length) throws IOException {
		socket.send(new DatagramPacket(bytes, offset, length, targetAddress));
	}

	@Override
	public void writeObject(Serializable obj) throws IOException {
		byte[] bytes = UDPUtils.serializeObject(obj);
		socket.send(new DatagramPacket(bytes, 0, bytes.length, targetAddress));
	}

	@Override
	public void writeUnshared(Serializable obj) throws IOException {
		writeObject(obj);
	}

	@Override
	public int read() throws IOException {
		byte[] receive = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(receive, receive.length);
		socket.receive(packet);

		return packet.getData()[0];
	}

	@Override
	public void read(byte[] arr) throws IOException {
		byte[] receive = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(receive, receive.length);
		socket.receive(packet);

		arr = packet.getData();
	}

	@Override
	public void read(byte[] arr, int off, int len) throws IOException {
		byte[] receive = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(receive, off, len);
		socket.receive(packet);

		arr = packet.getData();
	}

	@Override
	public Serializable readObject() throws IOException, ClassNotFoundException {
		byte[] receive = new byte[bufferSize];
		DatagramPacket packet = new DatagramPacket(receive, receive.length);
		socket.receive(packet);

		return UDPUtils.deserializeObject(packet.getData());
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

	public InetSocketAddress getTargetAddress() {
		return targetAddress;
	}

	public void setTargetAddress(InetSocketAddress targetAddress) {
		this.targetAddress = targetAddress;
	}
}
