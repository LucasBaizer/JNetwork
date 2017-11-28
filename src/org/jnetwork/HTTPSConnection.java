package org.jnetwork;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPSConnection extends HTTPConnection {
	public HTTPSConnection(String host) throws IOException {
		this(host, 443, new URLParameters());
	}

	public HTTPSConnection(String host, URLParameters params) throws IOException {
		this(host, 443, params);
	}

	public HTTPSConnection(String host, int port) throws IOException {
		this(host, port, new URLParameters());
	}

	public HTTPSConnection(String host, int port, URLParameters params) throws IOException {
		URL portless = new URL("https://" + host + params);
		url = new URL(portless.getProtocol(), portless.getHost(), port, portless.getFile());
		
		client = (HttpURLConnection) url.openConnection();
		client.setConnectTimeout(2000);
		client.setReadTimeout(2000);
	}
}
