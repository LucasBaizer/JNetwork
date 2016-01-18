package org.jnetwork.event;

import org.jnetwork.SocketPackage;

/**
 * Used in pair with a {@code Server}. When a client connects to the
 * {@code Server}, the {@code Server}'s {@code ClientConnectionListener} will be
 * called in a new thread.
 * 
 * @author Lucas Baizer
 */
public interface ClientConnectionListener extends NetworkListener {
	/**
	 * This method gets called on when a client connects to the
	 * <code>Server</code>.
	 * 
	 * @param event
	 *            - All the data required to read and write objects with the
	 *            client.
	 */
	public void clientConnected(SocketPackage event);
}