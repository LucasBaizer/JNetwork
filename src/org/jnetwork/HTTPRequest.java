package org.jnetwork;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class HTTPRequest {
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

		this.body = new String(buffer.toByteArray(), StandardCharsets.ISO_8859_1);
	}

	public String requestBody() {
		return this.body;
	}

	public HTTPHeader header(String name) {
		return new HTTPHeader(name, headers.get(name));
	}

	public HTTPHeader[] headers() {
		return HTTPHeader.extractFromHeaders(this.headers);
	}

	public String method() {
		return method;
	}
}
