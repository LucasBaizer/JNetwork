package example;

import java.io.StreamCorruptedException;

import org.jnetwork.CloseRequest;
import org.jnetwork.SecureUDPConnection;
import org.jnetwork.SecureUDPServer;
import org.jnetwork.SocketPackage;
import org.jnetwork.UDPUtils;
import org.jnetwork.listener.UDPConnectionListener;

public class SecureUDPObjectTransfer {
	/**
	 * Prints "Hey, secure server!" server-side and "Hey, secure client!"
	 * client-side.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			SecureUDPServer server = new SecureUDPServer(1337, new UDPConnectionListener() {
				@Override
				public void dataReceived(SocketPackage event, byte[] data) {
					try {
						try {
							System.out.println(Thread.currentThread().getName() + ": "
									+ (Object) UDPUtils.deserializeObject(data));
						} catch (StreamCorruptedException e) {
							System.out.print(Thread.currentThread().getName() + ": " + data.length + " bytes: ");
							for (int i = 0; i < 1024; i++) {
								System.out.print(data[i] + " ");
							}
							System.out.println();
						}

						event.getConnection().writeObject("Hey, secure client!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			});
			CloseRequest.addObjectToClose(server);
			server.start();

			SecureUDPConnection client = new SecureUDPConnection("localhost", 1337);
			client.writeObject("Hey, secure server!");

			System.out.println(Thread.currentThread().getName() + ": " + client.readObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
