package org.jnetwork;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class HTTPConnection extends SSLConnection {
	protected HttpURLConnection client;

	public static void setKeepAliveEnabled(boolean alive) {
		System.setProperty("http.keepAlive", Boolean.toString(alive));
	}

	HTTPConnection() {
	}

	public HTTPConnection(String host) throws IOException {
		this(host, 80);
	}

	public HTTPConnection(String host, int port) throws IOException {
		URL portless = new URL("http://" + host);
		URL url = new URL(portless.getProtocol(), portless.getHost(), port, portless.getFile());

		client = (HttpURLConnection) url.openConnection();
		client.setConnectTimeout(2000);
		client.setReadTimeout(2000);
	}

	public HTTPConnection method(String type) throws ProtocolException {
		client.setRequestMethod(type);
		return this;
	}

	public String method() {
		return client.getRequestMethod();
	}

	public HTTPConnection header(String header, String value) {
		client.setRequestProperty(header, value);
		return this;
	}

	public String header(String header) {
		return client.getRequestProperty(header);
	}

	public HTTPConnection contentType(String type) {
		return header("Content-Type", type);
	}

	public String contentType() {
		return header("Content-Type");
	}

	public HTTPConnection userAgent(String type) {
		return header("User-Agent", type);
	}

	public String userAgent() {
		return header("User-Agent");
	}

	public HTTPResult send() throws IOException {
		return send("");
	}

	public HTTPResult send(String data) throws IOException {
		client.setDoOutput(true);
		DataOutputStream out = new DataOutputStream(client.getOutputStream());
		out.writeBytes(data);
		out.flush();
		out.close();

		int code = client.getResponseCode();

		List<HTTPHeader> headers = new ArrayList<>();
		for (Entry<String, List<String>> entry : client.getHeaderFields().entrySet()) {
			headers.add(new HTTPHeader(entry.getKey(), entry.getValue()));
		}

		byte[] bytes = new byte[client.getContentLength()];
		(code >= 200 && code <= 299 ? client.getInputStream() : client.getErrorStream()).read(bytes);
		String body = new String(bytes, StandardCharsets.ISO_8859_1);

		return new HTTPResult(code, headers, body);
	}

	@Override
	public SocketAddress getRemoteSocketAddress() {
		throw new UnsupportedOperationException("getRemoteSocketAddress is not supported on HTTPSConnection objects");
	}

	@Override
	public SocketAddress getLocalSocketAddress() {
		throw new UnsupportedOperationException("getLocalSocketAddress is not supported on HTTPSConnection objects");
	}

	@Override
	public boolean isClosed() {
		throw new UnsupportedOperationException("isClosed is not supported on HTTPSConnection objects");
	}

	@Override
	public void write(int b) throws IOException {
		throw new UnsupportedOperationException("write is not supported on HTTPSConnection objects");
	}

	@Override
	public void write(byte[] bytes, int off, int len) throws IOException {
		throw new UnsupportedOperationException("write is not supported on HTTPSConnection objects");
	}

	@Override
	public void writeObject(Serializable obj) throws IOException {
		throw new UnsupportedOperationException("writeObject is not supported on HTTPSConnection objects");
	}

	@Override
	public int read() throws IOException {
		throw new UnsupportedOperationException("read is not supported on HTTPSConnection objects");
	}

	@Override
	public void read(byte[] arr, int off, int len) throws IOException {
		throw new UnsupportedOperationException("read is not supported on HTTPSConnection objects");
	}

	@Override
	public Serializable readObject() throws IOException, ClassNotFoundException {
		throw new UnsupportedOperationException("readObject is not supported on HTTPSConnection objects");
	}

	@Override
	public AdvancedOutputStream getOutputStream() {
		throw new UnsupportedOperationException("getOutputStream is not supported on HTTPSConnection objects");
	}

	@Override
	public AdvancedInputStream getInputStream() {
		throw new UnsupportedOperationException("getInputStream is not supported on HTTPSConnection objects");
	}

	@Override
	public void close() throws IOException {
		// throw new UnsupportedOperationException("close is not supported on
		// HTTPSConnection objects");
	}
}
