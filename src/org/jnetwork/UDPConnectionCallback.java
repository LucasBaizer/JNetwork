package org.jnetwork;

public interface UDPConnectionCallback extends ClientConnectionCallback {
	/**
	 * This method gets called on when a client sends UDP data to the
	 * <code>Server</code>.
	 * 
	 * @param event
	 *            - All the data required to write objects to the client.
	 * @param data
	 *            - The data sent to the server by the client.
	 */
	public void dataReceived(ClientData event, byte[] data);
}
