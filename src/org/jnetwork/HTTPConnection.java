package org.jnetwork;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.sun.net.httpserver.Headers;

public class HTTPConnection extends SSLConnection {
	protected HttpURLConnection client;
	protected URL url;
	private boolean cacheBody = true;
	private String cachedBody;
	private Headers headers = new Headers();

	public static void setKeepAliveEnabled(boolean alive) {
		System.setProperty("http.keepAlive", Boolean.toString(alive));
	}

	HTTPConnection() {
	}

	public HTTPConnection(String host) throws IOException {
		this(host, 80, new URLParameters());
	}

	public HTTPConnection(String host, URLParameters params) throws IOException {
		this(host, 80, params);
	}

	public HTTPConnection(String host, int port) throws IOException {
		this(host, port, new URLParameters());
	}

	public HTTPConnection(String host, int port, URLParameters params) throws IOException {
		URL portless = new URL("http://" + host + params.toString());
		url = new URL(portless.getProtocol(), portless.getHost(), port, portless.getFile());

		client = (HttpURLConnection) url.openConnection();
		client.setConnectTimeout(2000);
		client.setReadTimeout(2000);
	}

	public URL getURL() {
		return this.url;
	}

	public HTTPConnection method(String type) throws ProtocolException {
		client.setRequestMethod(type);
		return this;
	}

	public String method() {
		return client.getRequestMethod();
	}

	public HTTPConnection header(String header, String value) {
		headers.put(header, Arrays.asList(value));
		client.setRequestProperty(header, value);
		return this;
	}

	public String header(String header) {
		return headers.getFirst(header);
	}

	public HTTPHeader[] headers() {
		return HTTPHeader.extractFromHeaders(headers);
	}
	
	public HTTPConnection accept(String type) {
		return header(HTTPHeader.ACCEPT, type);
	}

	public String accept() {
		return header(HTTPHeader.ACCEPT);
	}

	public HTTPConnection contentType(String type) {
		return header(HTTPHeader.CONTENT_TYPE, type);
	}

	public String contentType() {
		return header(HTTPHeader.CONTENT_TYPE);
	}

	public HTTPConnection userAgent(String type) {
		return header(HTTPHeader.USER_AGENT, type);
	}

	public String userAgent() {
		return header(HTTPHeader.USER_AGENT);
	}

	public String getRequestString() {
		String result = method() + " " + url.getPath() + "\n";
		for (HTTPHeader header : HTTPHeader.extractFromMap(headers)) {
			result += header.toString() + "\n";
		}

		return result.substring(0, result.length() - 1);
	}

	public HTTPConnection authorization(HTTPAuthorization auth) {
		return header(HTTPHeader.AUTHORIZATION, auth.toString());
	}

	public HTTPAuthorization authorization() throws AuthorizationException {
		String auth = header(HTTPHeader.AUTHORIZATION);
		if (auth == null) {
			throw new AuthorizationException("No authorization was set");
		}
		return HTTPAuthorization.fromString(auth);
	}

	public HTTPResult get() throws IOException {
		method(HTTPMethodType.GET);
		return send(null);
	}

	public HTTPResult post(String data) throws IOException {
		method(HTTPMethodType.POST);
		return send(data);
	}

	public HTTPResult send() throws IOException {
		return send(null);
	}

	public HTTPResult send(String data) throws IOException {
		if (cacheBody) {
			cachedBody = data;
		}

		if (!method().equals(HTTPMethodType.GET)) {
			client.setDoOutput(true);
		} else {
			if (data != null) {
				throw new IOException("GET requests can not have bodies");
			}
		}
		client.setDoInput(true);

		if (!method().equals(HTTPMethodType.GET)) {
			DataOutputStream out = new DataOutputStream(client.getOutputStream());
			if (data == null) {
				data = "";
			}
			out.writeBytes(data);
			out.flush();
			out.close();
		}

		int code = client.getResponseCode();

		String body = "";
		if (client.getContentLength() > 0) {
			byte[] bytes = new byte[client.getContentLength()];
			(code >= 200 && code <= 299 ? client.getInputStream() : client.getErrorStream()).read(bytes);
			body = new String(bytes, StandardCharsets.UTF_8);
		}

		List<HTTPHeader> headers = new ArrayList<>();
		for (Entry<String, List<String>> entry : client.getHeaderFields().entrySet()) {
			headers.add(new HTTPHeader(entry.getKey(), entry.getValue()));
		}

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
	public int read() throws IOException {
		throw new UnsupportedOperationException("read is not supported on HTTPSConnection objects");
	}

	@Override
	public void read(byte[] arr, int off, int len) throws IOException {
		throw new UnsupportedOperationException("read is not supported on HTTPSConnection objects");
	}

	@Override
	public void close() throws IOException {
		// throw new UnsupportedOperationException("close is not supported on
		// HTTPSConnection objects");
	}

	public boolean isCacheBody() {
		return cacheBody;
	}

	public void setCacheBody(boolean cacheBody) {
		this.cacheBody = cacheBody;
	}

	public String getCachedBody() {
		return cachedBody;
	}
}
