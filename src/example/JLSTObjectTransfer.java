package example;

import org.jnetwork.CloseRequest;
import org.jnetwork.JLSTConnection;
import org.jnetwork.JLSTServer;
import org.jnetwork.SocketPackage;
import org.jnetwork.listener.TCPConnectionListener;

public class JLSTObjectTransfer {
	/**
	 * Prints "Hey, secure server!" server-side and "Hey, secure client!"
	 * client-side.
	 */
	public static void main(String[] args) {
		try {
			JLSTServer server = new JLSTServer(1337, new TCPConnectionListener() {
				@Override
				public void clientConnected(SocketPackage event) {
					try {
						System.out
								.println(Thread.currentThread().getName() + ": " + event.getConnection().readObject());

						event.getConnection().writeObject("Hey, secure client!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			CloseRequest.addObjectToClose(server);
			server.start();

			JLSTConnection client = new JLSTConnection("localhost", 1337);
			client.writeObject("Hey, secure server!");

			System.out.println(Thread.currentThread().getName() + ": " + client.readObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
