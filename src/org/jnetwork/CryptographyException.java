package org.jnetwork;

public class CryptographyException extends Exception {
	private static final long serialVersionUID = -7382959768483957637L;

	public CryptographyException() {
		super();
	}

	public CryptographyException(Throwable cause) {
		super(cause);
	}

	public CryptographyException(String msg) {
		super(msg);
	}

	public CryptographyException(String msg, Throwable cause) {
		super(msg, cause);
	}
}
