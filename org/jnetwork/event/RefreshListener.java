package org.jnetwork.event;

import org.jnetwork.SocketPackage;

/**
 * Used in pair with a {@code Server}. When the {@code refresh()} method is
 * called in a {@code Server}, the {@code Server} will call on every
 * {@code RefreshListener} added with {@code addRefreshListener()} for every
 * disconnected client.
 * 
 * @author Lucas Baizer
 */
public interface RefreshListener extends NetworkListener {
	/**
	 * This method is called whenever a client is removed by a refresh.
	 * 
	 * @param event
	 *            - All the data required to make the closing of the client
	 *            cleaner.
	 */
	public void clientDisconnect(SocketPackage event);
}