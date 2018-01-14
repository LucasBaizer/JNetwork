package org.jnetwork;

import java.io.IOException;

public class HTTPConnectionBuilder {
	private String host;
	private String method;
	private URLParameters params = new URLParameters();
	private boolean secure;
	private int port = 80;
	private boolean portOverride;

	private HTTPConnectionBuilder() {
	}

	public static HTTPConnectionBuilder get(String host) {
		HTTPConnectionBuilder to = to(host);
		to.method = HTTPMethodType.GET;
		return to;
	}

	public static HTTPConnectionBuilder post(String host) {
		HTTPConnectionBuilder to = to(host);
		to.method = HTTPMethodType.POST;
		return to;
	}

	public static HTTPConnectionBuilder to(String host) {
		HTTPConnectionBuilder builder = new HTTPConnectionBuilder();

		if (host.startsWith("http://")) {
			host = host.substring(7);
		} else if (host.startsWith("https://")) {
			host = host.substring(8);
			builder.secure = true;
			builder.port = 443;
		}

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

	@SuppressWarnings("resource")
	public <T extends HTTPConnection> T build() throws IOException {
		T conn = null;
		if (secure) {
			conn = (T) new HTTPSConnection(host, port, params);
		} else {
			conn = (T) new HTTPConnection(host, port, params);
		}

		if (method != null) {
			conn.method(method);
		}

		return conn;
	}
}
