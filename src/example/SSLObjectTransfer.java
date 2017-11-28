package example;

import java.io.File;
import java.io.IOException;

import org.jnetwork.CloseRequest;
import org.jnetwork.SSLConnection;
import org.jnetwork.Keystore;
import org.jnetwork.SSLServer;
import org.jnetwork.ClientData;
import org.jnetwork.TCPConnectionCallback;

public class SSLObjectTransfer {
	/**
	 * This code prints "Hello, from the client!" on server-side, and then
	 * "Hello, from the server!" on client-side. NOTE: THIS REQUIRES YOU TO MAKE
	 * A KEYSTORE!
	 **/
	public static void main(String[] args) {
		try {
			SSLServer server = new SSLServer(1337, new TCPConnectionCallback() {
				@Override
				public void clientConnected(ClientData event) {
					try {
						System.out.println(event.getConnection().getObjectInputStream().readObject());
						event.getConnection().getObjectOutputStream().writeObject("Hello, from the server!");
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}, new Keystore(new File("keystore.jks"), "password", "jnetwork", "password"));
			CloseRequest.addObjectToClose(server);
			server.start();
			SSLConnection connection = new SSLConnection("localhost", 1337);
			connection.getObjectOutputStream().writeObject("Hello, from the client!");
			System.out.println(connection.getObjectInputStream().readObject());

			connection.close();
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
