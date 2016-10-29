package org.jnetwork;

import java.io.IOException;

public class ServerException extends IOException {
	private static final long serialVersionUID = 6191107620801085607L;

	public ServerException() {
		super();
	}

	public ServerException(String msg) {
		super(msg);
	}

	public ServerException(String msg, Throwable cause) {
		super(msg, cause);
	}

	public ServerException(Throwable cause) {
		super(cause);
	}
}