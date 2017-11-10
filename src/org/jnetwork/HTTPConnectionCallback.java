package org.jnetwork;

import java.io.IOException;

public interface HTTPConnectionCallback extends ClientConnectionCallback {
	public void get(HTTPRequest req, HTTPResponse res) throws IOException;
}
