package example;

import java.io.IOException;
import java.net.UnknownHostException;

import org.jnetwork.CloseRequest;
import org.jnetwork.DataPackage;
import org.jnetwork.TCPConnection;

/**
 * This class reads an object in from a server and then sends an object back.
 */
public class TCPObjectTransferClient {
	public static void main(String[] args) {
		try {
			// connects to the server
			TCPConnection client = new TCPConnection("thehostaddress", 1337);

			// the client socket will be closed on program exit
			CloseRequest.addObjectToClose(client);

			// reads the first DataPackage sent my the server
			DataPackage packageFromServer = (DataPackage) client.getObjectInputStream().readObject();

			// prints out the received package's message, followed by the first
			// and second
			// pieces of data added to the package by the server
			System.out.println(packageFromServer.getMessage() + ": " + packageFromServer.getObjects()[0]
					+ " Your IP Address is " + packageFromServer.getObjects()[1]);

			// creates a new DataPackage containing a string saying "Hello,
			// Server." and a message
			// saying "ImportantPackageFromClient"
			DataPackage pkgOut = new DataPackage("Hello, server!").setMessage("ImportantPackageFromClient");
			client.getObjectOutputStream().writeObject(pkgOut);

			// closes the client and all of its streams
			client.close();
		} catch (UnknownHostException e) {
			System.err.println("The host doesn't exist!");
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("An I/O error occurred while connecting to the server.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("The server sent an object who's type does not exist on this machine.");
			e.printStackTrace();
		}

		/**
		 * OUTPUT: ImportantPackageFromServer: Hello, client! Your IP Address is
		 * [the IP address].
		 */
	}
}