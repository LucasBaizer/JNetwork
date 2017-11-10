package org.jnetwork;

import java.io.IOException;
import java.net.InetSocketAddress;

import com.sun.net.httpserver.HttpServer;

public class HTTPServer extends TCPServer {
	private HttpServer server;

	public HTTPServer() {
		this(80);
	}

	public HTTPServer(int port) {
		super(port, null);
	}

	@Override
	public void start() throws IOException {
		server = HttpServer.create(new InetSocketAddress(getBoundPort()), 0);
		server.setExecutor(null);
		server.start();
	}

	public HTTPServer get(String uri, HTTPConnectionCallback back) throws ServerException {
		if (server == null) {
			throw new ServerException("Server must be started before endpoints are configured");
		}

		server.createContext(uri, (http) -> {
			HTTPRequest req = new HTTPRequest(http);
			HTTPResponse res = new HTTPResponse(http);
			back.get(req, res);
		});
		return this;
	}

	@Override
	protected void launchNewThread() throws IOException {
		// this doesn't matter
	}

	@Override
	public void close() throws IOException {
	}
}
