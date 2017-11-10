package org.jnetwork;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;

public class URLParameters {
	private HashMap<String, String> params = new HashMap<>();

	public URLParameters() {
	}

	public URLParameters(String... params) {
		if (params.length % 2 != 0) {
			throw new IllegalArgumentException("param key is missing a value");
		}

		for (int i = 0; i < params.length; i += 2) {
			this.params.put(params[i], params[i + 1]);
		}
	}

	public URLParameters(String params) {
		if (params.startsWith("?")) {
			params = params.substring(1);
		}
		String[] split = params.split(Pattern.quote("&"));
		for (String par : split) {
			String[] parSplit = par.split(Pattern.quote("="));
			this.params.put(parSplit[0], parSplit[1]);
		}
	}

	public URLParameters(Map<String, String> params) {
		this.params = new HashMap<>(params);
	}

	public URLParameters addParameter(String param, String val) {
		params.put(param, val);
		return this;
	}

	public URLParameters removeParameter(String param) {
		params.remove(param);
		return this;
	}

	public boolean containsParameter(String param) {
		return params.containsKey(param);
	}

	@Override
	public String toString() {
		if (params.size() == 0) {
			return "";
		}

		String str = "?";
		for (Entry<String, String> entry : params.entrySet()) {
			str += entry.getKey() + "=" + entry.getValue() + "&";
		}
		return str.substring(0, str.length() - 1);
	}
}
