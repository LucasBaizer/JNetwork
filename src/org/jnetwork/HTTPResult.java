package org.jnetwork;

import java.io.IOException;
import java.util.List;

public class HTTPResult {
	private String body;
	private int code;
	private List<HTTPHeader> headers;

	HTTPResult(int code, List<HTTPHeader> headers, String body) throws IOException {
		this.code = code;
		this.headers = headers;
		this.body = body;
	}

	public String getBody() {
		return this.body;
	}

	public HTTPHeader getHeader(String name) {
		for (HTTPHeader header : this.headers) {
			if (header.getName().equals(name)) {
				return header;
			}
		}
		return null;
	}

	public HTTPHeader[] getHeaders() {
		return this.headers.toArray(new HTTPHeader[this.headers.size()]);
	}

	public int getResponseCode() {
		return this.code;
	}
	
	@Override
	public String toString() {
		String result = "";
		for(HTTPHeader header : headers) {
			result += header.toString() + System.lineSeparator();
		}
		
		result += System.lineSeparator() + body;
		return result;
	}
}
