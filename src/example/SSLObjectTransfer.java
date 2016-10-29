package example;

import java.io.File;
import java.io.IOException;

import org.jnetwork.CloseRequest;
import org.jnetwork.SSLConnection;
import org.jnetwork.Keystore;
import org.jnetwork.SSLServer;
import org.jnetwork.SocketPackage;
import org.jnetwork.TCPConnectionListener;

public class SSLObjectTransfer {
	/**
	 * This code prints "Hello, from the client!" on server-side, and then
	 * "Hello, from the server!" on client-side. NOTE: THIS REQUIRES YOU TO MAKE
	 * A KEYSTORE!
	 **/
	public static void main(String[] args) {
		try {
			SSLServer server = new SSLServer(new Keystore(new File("keystore.jks"), "password"), 1337,
					new TCPConnectionListener() {
						@Override
						public void clientConnected(SocketPackage event) {
							try {
								System.out.println(event.getConnection().readObject());
								event.getConnection().writeObject("Hello, from the server!");
							} catch (ClassNotFoundException e) {
								e.printStackTrace();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
					});
			CloseRequest.addObjectToClose(server);
			server.start();
			SSLConnection connection = new SSLConnection("localhost", 1337);
			connection.writeObject("Hello, from the client!");
			System.out.println(connection.readObject());

			connection.close();
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
