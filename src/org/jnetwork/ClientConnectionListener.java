package org.jnetwork;

/**
 * Used in pair with a {@code Server}. When a client connects to the
 * {@code Server}, the {@code Server}'s {@code ClientConnectionListener} will be
 * called in a new thread.
 * 
 * @author Lucas Baizer
 */
public interface ClientConnectionListener extends NetworkListener {
}