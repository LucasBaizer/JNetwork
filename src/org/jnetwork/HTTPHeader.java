package org.jnetwork;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;

import com.sun.net.httpserver.Headers;

public class HTTPHeader {
	public static final String CONTENT_TYPE = "Content-Type";
	public static final String ACCESS_ALLOW_CONTROL_ORIGIN = "Access-Allow-Control-Origin";
	public static final String ACCEPT = "Accept";
	public static final String ACCEPT_ENCODING = "Accept-Encoding";
	public static final String USER_AGENT = "User-Agent";
	public static final String CONNECTION = "Connection";
	public static final String HOST = "Host";
	public static final String ACCEPT_LANGUAGE = "Accept-Language";
	public static final String AUTHORIZATION = "Authorization";

	private String name;
	private List<String> values;

	static HTTPHeader[] extractFromHeaders(Headers headers) {
		HTTPHeader[] array = new HTTPHeader[headers.size()];

		int index = 0;
		for (Entry<String, List<String>> entry : headers.entrySet()) {
			array[index++] = new HTTPHeader(entry.getKey(), entry.getValue());
		}

		return array;
	}

	public HTTPHeader(String name) {
		this(name, new ArrayList<>());
	}

	public HTTPHeader(String name, String value) {
		this(name, new ArrayList<>(Arrays.asList(value)));
	}

	public HTTPHeader(String name, String... values) {
		this(name, new ArrayList<>(Arrays.asList(values)));
	}

	public HTTPHeader(String name, List<String> values) {
		this.name = name;
		this.values = values == null ? new ArrayList<>() : values;
	}

	public String getName() {
		return this.name;
	}

	public String getFirstValue() {
		return this.isValidHeader() ? this.values.get(0) : null;
	}

	public List<String> getValues() {
		return this.values;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.values = new ArrayList<>(Arrays.asList(value));
	}

	public void setValues(List<String> values) {
		this.values = values;
	}

	public void addValue(String value) {
		this.values.add(value);
	}

	public boolean containsValue(String value) {
		return this.values.contains(value);
	}

	public void removeValue(String value) {
		this.values.remove(value);
	}
	
	public boolean isValidHeader() {
		return this.values.size() > 0;
	}

	@Override
	public String toString() {
		if (name == null) {
			return values.toString();
		} else {
			return name + ": " + values.toString();
		}
	}
}
