package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;
import java.net.SocketAddress;
import java.net.URL;

import com.sun.net.httpserver.HttpExchange;

import sun.net.www.http.HttpClient;

public class HTTPSConnection extends SSLConnection {
	protected AdvancedOutputStream out;
	protected AdvancedInputStream in;
	private SocketAddress remote;
	private SocketAddress local;

	public HTTPSConnection(String host) throws IOException {
		this(host, 443);
	}

	public HTTPSConnection(String host, int port) throws IOException {
		HttpClient client = HttpClient.New(new URL("https://" + host + ":" + port));
		client.afterConnect();

		out = new AdvancedOutputStream(client.getOutputStream());
		in = new AdvancedInputStream(client.getInputStream());
	}

	HTTPSConnection(HttpExchange http) throws IOException {
		out = new AdvancedOutputStream(http.getResponseBody());
		in = new AdvancedInputStream(http.getRequestBody());
		remote = http.getRemoteAddress();
		local = http.getLocalAddress();
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		return remote;
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		return local;
	}

	@Override
	public boolean isClosed() {
		throw new UnsupportedOperationException("isClosed is not supported on HTTPSConnection objects");
	}

	@Override
	public void write(int b) throws IOException {
		out.write(b);
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		out.write(bytes, off, len);
	}

	@Override
	public void writeObject(Serializable obj) throws IOException {
		out.writeObject(obj);
	}

	@Override
	public int read() throws IOException {
		return in.read();
	}

	@Override
	public void read(byte[] arr, int off, int len) throws IOException {
		in.read(arr, off, len);
	}

	@Override
	public Serializable readObject() throws IOException, ClassNotFoundException {
		return (Serializable) in.readObject();
	}

	@Override
	public AdvancedOutputStream getOutputStream() {
		return out;
	}

	@Override
	public AdvancedInputStream getInputStream() {
		return in;
	}
}
