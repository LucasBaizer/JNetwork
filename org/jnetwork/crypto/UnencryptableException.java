package org.jnetwork.crypto;

/**
 * An exception to be thrown if an object that implements the
 * <code>Unencryptable</code> interface is being encrypted.
 * 
 * @see javax.network.crypto.Unencryptable
 * 
 * @author Lucas Baizer
 */
public class UnencryptableException extends Exception {
	private static final long serialVersionUID = -4085197416645453294L;

	public UnencryptableException(String message) {
		super(message);
	}
}