package org.jnetwork;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPSConnection extends HTTPConnection {
	public HTTPSConnection(String host) throws IOException {
		this(host, 443);
	}

	public HTTPSConnection(String host, int port) throws IOException {
		URL portless = new URL("https://" + host);
		URL url = new URL(portless.getProtocol(), portless.getHost(), port, portless.getFile());

		client = (HttpURLConnection) url.openConnection();
		client.setConnectTimeout(2000);
		client.setReadTimeout(2000);
	}
}
