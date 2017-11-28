package example;

import java.io.File;

import org.jnetwork.ClientData;
import org.jnetwork.CloseRequest;
import org.jnetwork.Keystore;
import org.jnetwork.TCPConnectionCallback;

public class JLSTObjectTransfer {
	/**
	 * Prints "Hey, secure server!" server-side and "Hey, secure client!"
	 * client-side.
	 */
	public static void main(String[] args) {
		try {
			JLSTServer server = new JLSTServer(1337, new TCPConnectionCallback() {
				@Override
				public void clientConnected(ClientData event) {
					try {
						System.out.println(Thread.currentThread().getName() + ": "
								+ event.getConnection().getObjectInputStream().readObject());

						event.getConnection().getObjectOutputStream().writeObject("Hey, secure client!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, new Keystore(new File("keystore.jks"), "password", "jnetwork", "password"));
			CloseRequest.addObjectToClose(server);
			server.start();

			JLSTConnection client = new JLSTConnection("localhost", 1337);
			client.getObjectOutputStream().writeObject("Hey, secure server!");

			System.out.println(Thread.currentThread().getName() + ": " + client.getObjectInputStream().readObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
