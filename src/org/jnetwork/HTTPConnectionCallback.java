package org.jnetwork;

import java.io.IOException;

public interface HTTPConnectionCallback extends ClientConnectionCallback {
	public int get(HTTPRequest req, HTTPResponse res) throws IOException;
}
