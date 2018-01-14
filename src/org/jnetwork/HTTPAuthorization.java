package org.jnetwork;

public abstract class HTTPAuthorization {
	private String type;
	private String value;

	public HTTPAuthorization(String type) {
		this.type = type;
	}

	public static <T extends HTTPAuthorization> T fromHeader(HTTPHeader header) throws AuthorizationException {
		return fromString(header.getFirstValue());
	}

	public static <T extends HTTPAuthorization> T fromString(String str) throws AuthorizationException {
		if (!str.contains(" ")) {
			throw new WebException("Invalid authorization");
		}
		String[] split = str.split(" ", 2);
		if (split[0].equalsIgnoreCase("basic")) {
			return (T) new HTTPBasicAuthorization(split[1]);
		}
		throw new WebException("No implementation for authorization: " + split[0]);
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return this.type;
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		return type + " " + value;
	}
}
