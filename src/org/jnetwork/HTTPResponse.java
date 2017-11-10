package org.jnetwork;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;

public class HTTPResponse {
	private static final Headers defaultHeaders = new Headers();

	private HttpExchange http;
	private Headers headers;
	private int responseCode = 200;

	HTTPResponse(HttpExchange http) throws IOException {
		this.http = http;
		this.headers = http.getResponseHeaders();
	}

	public static HTTPHeader[] getDefaultHeaders() {
		return HTTPHeader.extractFromHeaders(defaultHeaders);
	}

	public static HTTPHeader getDefaultHeader(String header) {
		return new HTTPHeader(header, defaultHeaders.get(header));
	}

	public static void addDefaultHeader(HTTPHeader header) {
		defaultHeaders.put(header.getName(), header.getValues());
	}

	public static void removeDefaultHeader(String name) {
		defaultHeaders.remove(name);
	}

	public String header(String name) {
		return headers.get(name).get(0);
	}

	public HTTPResponse header(HTTPHeader header) {
		headers.put(header.getName(), header.getValues());
		return this;
	}

	public HTTPHeader[] headers() {
		return HTTPHeader.extractFromHeaders(this.headers);
	}

	public HTTPResponse code(int code) {
		this.responseCode = code;
		return this;
	}

	public int code() {
		return this.responseCode;
	}

	public HTTPResponse send(File file) throws IOException {
		return send(new String(Files.readAllBytes(file.toPath()), StandardCharsets.ISO_8859_1));
	}

	public HTTPResponse send(String responseText) throws IOException {
		byte[] bytes = responseText.getBytes(StandardCharsets.ISO_8859_1);
		headers.putAll(defaultHeaders);
		http.sendResponseHeaders(responseCode, bytes.length);
		DataOutputStream out = new DataOutputStream(http.getResponseBody());
		out.write(bytes);
		out.flush();
		http.close();

		return this;
	}

	public HTTPResponse contentType(String type) {
		headers.put("Content-Type", Arrays.asList(type));
		return this;
	}

	public String contentType() {
		return this.headers.get("Content-Type").get(0);
	}
}
