package org.jnetwork;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;

import org.jnetwork.listener.UDPConnectionListener;

/**
 * A UDP representation of the Server object. Used for sending and receiving
 * data with UDPConnection objects.
 * 
 * @author Lucas Baizer
 */
public class UDPServer extends Server {
	protected DatagramSocket server;
	protected int bufferSize = 1024;

	public UDPServer(int port, UDPConnectionListener clientSocketThread) {
		super(port, clientSocketThread);
	}

	@Override
	public void start() throws IOException {
		this.server = new DatagramSocket(getBoundPort());

		super.startDispatch();
	}

	@Override
	protected void launchNewThread() throws IOException, InterruptedException {
		byte[] receiveData = new byte[bufferSize];
		DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
		server.receive(receivePacket);

		UDPConnection conn = new UDPConnection(server);
		conn.setTargetAddress((InetSocketAddress) receivePacket.getSocketAddress());
		final SocketPackage event = new SocketPackage(conn);

		refresh();
		clients.add(event);
		refresh();

		Thread thr = new Thread(new Runnable() {
			@Override
			public void run() {
				((UDPConnectionListener) getClientConnectionListener()).dataReceived(event, receivePacket.getData());
				try {
					removeClient(event);
				} catch (IOException e) {
					Thread.currentThread().getUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
				}
			}
		}, "JNetwork-UDPServer-Thread-" + receivePacket.getSocketAddress());
		event.setHoldingThread(thr);
		thr.start();

		launchNewThread();
	}

	/**
	 * @return the internal <code>DatagramSocket</code> the <code>Server</code>
	 *         is built off of.
	 */
	public DatagramSocket getServerSocket() {
		return server;
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
}
