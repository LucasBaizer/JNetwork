package org.jnetwork;

/**
 * An enumeration of protocols, each one corresponding to a
 * {@link NetworkFactory};
 * 
 * @author Lucas Baizer
 */
public enum Protocol {
	TCP(new TCPNetworkFactory(), "Transmission Control Protocol"), UDP(new UDPNetworkFactory(),
			"User Datagram Protocol"), SSL(new SSLNetworkFactory(), "Secure Socket Layer");

	private NetworkFactory factory;
	private String protocolName;

	Protocol(NetworkFactory factory, String protocolName) {
		this.factory = factory;
		this.protocolName = protocolName;
	}

	/**
	 * @return The NetworkFactory associated with the protocol.
	 */
	public NetworkFactory getNetworkFactory() {
		return factory;
	}

	/**
	 * @return The name of the protocol, e.x. "Transport Control Protocol".
	 */
	public String getProtocolName() {
		return protocolName;
	}
}
