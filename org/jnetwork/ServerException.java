package org.jnetwork;

import java.io.IOException;

public class ServerException extends IOException {
	private static final long serialVersionUID = 6191107620801085607L;

	public ServerException(String msg) {
		super(msg);
	}
}