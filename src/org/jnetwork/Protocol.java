package org.jnetwork;

/**
 * An enumeration of protocols, each one corresponding to a
 * {@link NetworkFactory};
 * 
 * @author Lucas Baizer
 */
public enum Protocol {
	TCP(new TCPNetworkFactory()), UDP(new UDPNetworkFactory()), SSL(new SSLNetworkFactory()), SDTP(
			new SDTPNetworkFactory());

	private NetworkFactory factory;

	Protocol(NetworkFactory factory) {
		this.factory = factory;
	}

	/**
	 * @return The NetworkFactory associated with the protocol.
	 */
	public NetworkFactory getNetworkFactory() {
		return factory;
	}
}
