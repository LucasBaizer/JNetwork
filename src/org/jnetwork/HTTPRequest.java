package org.jnetwork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class HTTPRequest {
	private HashMap<String, String> params = new HashMap<>();
	private String body;
	private String method;
	private Headers headers;

	HTTPRequest(HttpExchange http) throws IOException {
		this.headers = http.getRequestHeaders();
		this.method = http.getRequestMethod();

		InputStream in = http.getRequestBody();
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read;
		byte[] data = new byte[16384];
		while ((read = in.read(data, 0, data.length)) != -1) {
			buffer.write(data, 0, read);
		}

		buffer.flush();

		this.body = new String(buffer.toByteArray(), StandardCharsets.UTF_8);

		String query = http.getRequestURI().getQuery();
		if (query != null) {
			for (String param : query.split("&")) {
				String pair[] = param.split("=");
				if (pair.length > 1) {
					this.params.put(pair[0], pair[1]);
				} else {
					this.params.put(pair[0], "");
				}
			}
		}
	}

	public String requestBody() {
		return this.body;
	}

	public boolean hasHeader(String name) {
		return this.headers.containsKey(name);
	}

	public HTTPHeader header(String name) {
		return new HTTPHeader(name, this.headers.get(name));
	}

	public HTTPHeader[] headers() {
		return HTTPHeader.extractFromHeaders(this.headers);
	}

	public String param(String name) {
		return this.params.get(name);
	}

	public Map<String, String> params() {
		return this.params;
	}

	public boolean hasParam(String name) {
		return this.params.containsKey(name);
	}

	public String method() {
		return method;
	}
}
