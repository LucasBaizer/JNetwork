package org.jnetwork;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.regex.Pattern;

public class HTTPBasicAuthorization extends HTTPAuthorization {
	private String username;
	private String password;

	public HTTPBasicAuthorization(String base64) {
		super("Basic");

		String[] split = new String(Base64.getDecoder().decode(base64), StandardCharsets.ISO_8859_1)
				.split(Pattern.quote(":"));
		this.username = split[0];
		this.password = split[1];
		this.setBase64();
	}

	public HTTPBasicAuthorization(String username, String password) {
		super("Basic");

		this.username = username;
		this.password = password;
		this.setBase64();
	}

	private void setBase64() {
		setValue(new String(
				Base64.getEncoder().encode((username + ":" + password).getBytes(StandardCharsets.ISO_8859_1)),
				StandardCharsets.ISO_8859_1));
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
		this.setBase64();
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
		this.setBase64();
	}

	public boolean matches(HTTPBasicAuthorization auth) {
		return matches(auth.username, auth.password);
	}

	public boolean matches(String username, String password) {
		return username.equals(this.username) && password.equals(this.password);
	}
}
