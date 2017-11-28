package org.jnetwork;

public interface ConnectionHandler<T> {
	public boolean handle(Server server, T client);
}
