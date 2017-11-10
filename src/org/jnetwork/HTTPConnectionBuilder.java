package org.jnetwork;

import java.io.IOException;

public class HTTPConnectionBuilder {
	private String host;
	private URLParameters params = new URLParameters();
	private boolean secure;
	private int port = 80;
	private boolean portOverride;

	private HTTPConnectionBuilder() {
	}

	public static HTTPConnectionBuilder to(String host) {
		HTTPConnectionBuilder builder = new HTTPConnectionBuilder();
		builder.host = host;
		return builder;
	}

	public HTTPConnectionBuilder secure() {
		this.secure = true;
		if (!this.portOverride) {
			this.port = 443;
		}
		return this;
	}

	public HTTPConnectionBuilder port(int port) {
		this.port = port;
		this.portOverride = true;
		return this;
	}

	public HTTPConnectionBuilder params(URLParameters params) {
		this.params = params;
		return this;
	}

	public <T extends HTTPConnection> T build() throws IOException {
		if (secure) {
			return (T) new HTTPSConnection(host, port, params);
		} else {
			return (T) new HTTPConnection(host, port, params);
		}
	}
}
