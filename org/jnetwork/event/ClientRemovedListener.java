package org.jnetwork.event;

import org.jnetwork.SocketPackage;

/**
 * Used in pair with a {@code Server}. When the {@code removeClientt()} method
 * is called in a {@code Server}, the {@code Server} will call on every
 * {@code ClientRemovedListener} added with {@code addClientRemovedListenerr()}
 * for the removed client.
 *
 * @author Lucas Baizer
 */
public interface ClientRemovedListener extends NetworkListener {
	/**
	 * This method gets called on whenever a client is removed from the
	 * <code>Server</code> forcefully by the server (not the client).
	 * 
	 * @param event
	 *            - All the data required to make the closing of the client
	 *            cleaner.
	 */
	public void clientRemoved(SocketPackage event);
}