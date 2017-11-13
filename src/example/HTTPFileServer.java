package example;

import java.io.File;
import java.io.IOException;

import org.jnetwork.HTTPServer;

public class HTTPFileServer {
	public static void main(String[] args) {
		HTTPServer server = new HTTPServer();

		try {
			server.start();
			server.serveDirectory(new File("www"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
