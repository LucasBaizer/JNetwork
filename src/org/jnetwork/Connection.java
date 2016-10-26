package org.jnetwork;

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.net.SocketAddress;

/**
 * An abstract class used for writing objects out to and reading objects from a
 * server. For TCP, use TCPConnection. For UDP, use UDPConnection.
 * 
 * @see java.net.Socket
 * 
 * @author Lucas Baizer
 */
public abstract class Connection implements Closeable {
	private Object closeWaiter = new Object();
	private String hostAddress;
	private int hostPort;

	Connection() {
	}

	public Connection(String host, int port) {
		this.hostAddress = host;
		this.hostPort = port;
	}

	/**
	 * Gets the remote socket address of the <code>Socket</code>.
	 * 
	 * @return The <code>SocketAddress</code>.
	 */
	public abstract SocketAddress getRemoteSocketAddress();

	/**
	 * Gets the local socket address of the <code>Socket</code>.
	 * 
	 * @return The <code>SocketAddress</code>.
	 */
	public abstract SocketAddress getLocalSocketAddress();

	/**
	 * Gets if the <code>Socket</code> is closed.
	 * 
	 * @return If the <code>Socket</code> is closed.
	 */
	public abstract boolean isClosed();

	/**
	 * See {@link java.io.OutputStream#write(int)}.
	 */
	public abstract void write(int b) throws IOException;

	/**
	 * See {@link java.io.OutputStream#write(byte[])}.
	 */
	public void write(byte[] bytes) throws IOException {
		write(bytes, 0, bytes.length);
	}

	/**
	 * See {@link java.io.OutputStream#write(byte[], int, int)}.
	 */
	public abstract void write(byte[] bytes, int off, int len) throws IOException;

	/**
	 * See {@link java.io.ObjectOutputStream#writeObject(Object)}.
	 */
	public abstract void writeObject(Serializable obj) throws IOException;

	/**
	 * See {@link java.io.ObjectOutputStream#writeUnshared(Object)}.
	 */
	public abstract void writeUnshared(Serializable obj) throws IOException;

	/**
	 * See {@link java.io.InputStream#read()}.
	 */
	public abstract int read() throws IOException;

	/**
	 * See {@link java.io.InputStream#read(byte[])}.
	 */
	public void read(byte[] arr) throws IOException {
		read(arr, 0, arr.length);
	}

	/**
	 * See {@link java.io.InputStream#read(byte[], int, int)}.
	 */
	public abstract void read(byte[] arr, int off, int len) throws IOException;

	/**
	 * See {@link java.io.ObjectInputStream#readObject(Object)}.
	 */
	public abstract Serializable readObject() throws IOException, ClassNotFoundException;

	/**
	 * See {@link java.io.ObjectInputStream#readUnshared(Object)}.
	 */
	public abstract Serializable readUnshared() throws IOException, ClassNotFoundException;

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
		synchronized (closeWaiter) {
			closeWaiter.notifyAll();
		}
	}

	/**
	 * Returns the <code>Socket</code>'s remote socket address.
	 */
	@Override
	public String toString() {
		return getRemoteSocketAddress().toString();
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

	/**
	 * @return The address of the host.
	 */
	public String getHostAddress() {
		return hostAddress;
	}

	/**
	 * @return The port of the host.
	 */
	public int getHostPort() {
		return hostPort;
	}
}