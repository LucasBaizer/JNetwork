package org.jnetwork;

import java.io.IOException;

import com.sun.net.httpserver.HttpsServer;

public class HTTPSServer extends SSLServer {
	private HttpsServer server;

	public HTTPSServer(Keystore keystore) {
		this(443, keystore);
	}

	public HTTPSServer(int port, Keystore keystore) {
		super(port, null, keystore);
	}

	@Override
	public void start() throws IOException {
		server = HttpsServer.create();
	}

	public HTTPSServer get(String uri, HTTPConnectionCallback back) {
		server.createContext(uri, (http) -> {
			Thread thread = new Thread(() -> {
				try {
					HTTPSConnection client = new HTTPSConnection(http);
					back.get(new ClientData(client, Thread.currentThread()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			});
			thread.setName("JNetwork-HTTPSServer-Thread-" + http.getRemoteAddress());
			thread.start();
		});
		return this;
	}

	@Override
	protected void launchNewThread() throws IOException {
		// this doesn't matter
	}
}
