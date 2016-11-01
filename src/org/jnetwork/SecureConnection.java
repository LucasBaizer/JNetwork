package org.jnetwork;

import java.io.IOException;
import java.io.Serializable;

public interface SecureConnection {
	public void writeUnencrypted(int v) throws IOException;

	public default void writeUnencrypted(byte[] arr) throws IOException {
		writeUnencrypted(arr, 0, arr.length);
	}

	public void writeUnencrypted(byte[] arr, int off, int len) throws IOException;

	/**
	 * Writes an object without encrypting it.
	 * 
	 * @param obj
	 *            - The object to send.
	 * @throws IOException
	 *             If an error occurs while writing the object.
	 */
	public void writeUnencryptedObject(Serializable obj) throws IOException;

	/**
	 * Reads an object without decrypting it.
	 * 
	 * @return The undecrypted object.
	 * @throws ClassNotFoundException
	 *             If the class of the object cannot be found.
	 * @throws IOException
	 *             If an error occurs while reading the object.
	 */
	public Serializable readUnencryptedObject() throws IOException, ClassNotFoundException;

	public int readUnencrypted() throws IOException;

	public default void readUnencrypted(byte[] arr) throws IOException {
		readUnencrypted(arr, 0, arr.length);
	}

	public void readUnencrypted(byte[] arr, int off, int len) throws IOException;
}
