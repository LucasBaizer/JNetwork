package example;

import java.io.File;

import org.jnetwork.HTTPServer;

public class HTTPFileServer {
	public static void main(String[] args) {
		HTTPServer server = new HTTPServer();

		try {
			server.start();
			server.serveDirectory(new File("www"));
			server.waitUntilClose();
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
