package example;

import java.io.IOException;

import org.jnetwork.CloseRequest;
import org.jnetwork.SocketPackage;
import org.jnetwork.UDPConnection;
import org.jnetwork.UDPServer;
import org.jnetwork.UDPUtils;
import org.jnetwork.listener.UDPConnectionListener;

public class UDPObjectTransfer {
	public static void main(String[] args) {
		try {
			UDPServer server = new UDPServer(1337, new UDPConnectionListener() {
				@Override
				public void dataReceived(SocketPackage event, byte[] data) {
					try {
						System.out.println((String) UDPUtils.deserializeObject(data));
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
			client.writeObject("Hello, world!");

			server.waitUntilClose();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
