package example;

import java.io.File;
import java.io.StreamCorruptedException;

import org.jnetwork.CloseRequest;
import org.jnetwork.Keystore;
import org.jnetwork.SDTPConnection;
import org.jnetwork.SDTPServer;
import org.jnetwork.SocketPackage;
import org.jnetwork.UDPConnectionListener;
import org.jnetwork.UDPUtils;

public class SDTPObjectTransfer {
	/**
	 * Prints "Hey, secure server!" server-side and "Hey, secure client!"
	 * client-side.
	 */
	public static void main(String[] args) {
		try {
			SDTPServer server = new SDTPServer(1337, new UDPConnectionListener() {
				@Override
				public void dataReceived(SocketPackage event, byte[] data) {
					try {
						try {
							System.out.println(Thread.currentThread().getName() + ": "
									+ (Object) UDPUtils.deserializeObject(data));
						} catch (StreamCorruptedException e) {
							System.out.print(Thread.currentThread().getName() + ": " + data.length + " bytes: ");
							for (int i = 0; i < Math.min(data.length, 1024); i++) {
								System.out.print(data[i] + " ");
							}
							System.out.println();
						}

						event.getConnection().writeObject("Hey, secure client!");
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}, new Keystore(new File("keystore.jks"), "password", "jnetwork", "password"));
			CloseRequest.addObjectToClose(server);
			server.start();

			SDTPConnection client = new SDTPConnection("localhost", 1337);
			client.writeObject("Hey, secure server!");

			System.out.println(Thread.currentThread().getName() + ": " + client.readObject());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
