package example;

import java.io.IOException;

import org.jnetwork.CloseRequest;
import org.jnetwork.SocketPackage;
import org.jnetwork.UDPConnection;
import org.jnetwork.UDPServer;
import org.jnetwork.UDPUtils;
import org.jnetwork.listener.UDPConnectionListener;

public class UDPObjectTransfer {
	/**
	 * This code prints "Hello, from the client!" on server-side, and then
	 * "Hello, from the server!" on client-side.
	 **/
	public static void main(String[] args) {
		try {
			UDPServer server = new UDPServer(1337, new UDPConnectionListener() {
				@Override
				public void dataReceived(SocketPackage event, byte[] data) {
					try {
						System.out.println((String) UDPUtils.deserializeObject(data));

						event.getConnection().writeObject("Hello, from the server!");
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			});
			server.start();
			CloseRequest.addObjectToClose(server);

			UDPConnection client = new UDPConnection("localhost", 1337);
			client.writeObject("Hello, from the client!");

			System.out.println(client.readObject());

			client.close();
			server.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
