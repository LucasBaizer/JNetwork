package example;

import java.io.IOException;
import java.net.SocketAddress;

import org.jnetwork.ClientData;
import org.jnetwork.CloseRequest;
import org.jnetwork.DataPackage;
import org.jnetwork.Server;
import org.jnetwork.TCPConnectionCallback;
import org.jnetwork.TCPServer;

/**
 * This class sends an object to each client that connects and then reads an
 * object in from the client.
 */
public class TCPObjectTransferServer implements TCPConnectionCallback {
	private static Server server;

	public static void main(String[] args) throws IOException {
		// creates a new server on port 1337 with the ClientConnectionCallback
		// as an instance of this class
		server = new TCPServer(1337, new TCPObjectTransferServer());

		// the server socket will be closed on program exit
		CloseRequest.addObjectToClose(server);
	}

	// this method is called each time a client connects
	@Override
	public void clientConnected(ClientData event) {
		// gets the IP address of the client
		SocketAddress address = event.getConnection().getRemoteSocketAddress();

		// creates a new DataPackage containing a string saying "Hello, client."
		// and the client's IP address. it also gives the package a message
		// saying "ImportantPackageFromServer"
		DataPackage pkg = new DataPackage("Hello, client!", address).setMessage("ImportantPackageFromServer");

		try {
			// writes the package out to the client
			event.getConnection().getObjectOutputStream().writeObject(pkg);
		} catch (IOException e) {
			System.err.println("There was an error sending the object to the client.");
			e.printStackTrace();
		}

		try {
			// reads the first DataPackage sent by the client
			DataPackage packageFromClient = (DataPackage) event.getConnection().getObjectInputStream().readObject();

			// prints out the received package's message, followed by the first
			// piece of data added to the package by the client
			System.out.println(packageFromClient.getMessage() + ": " + packageFromClient.getObjects()[0]);
		} catch (IOException e) {
			System.err.println("There was an I/O error reading the package.");
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			System.err.println("The client sent an object who's type does not exist on this machine.");
			e.printStackTrace();
		}

		// the method exits, all streams are closed
		// automatically, along with the client socket

		/**
		 * OUTPUT: ImportantPackageFromClient: Hello, server!
		 */
	}
}