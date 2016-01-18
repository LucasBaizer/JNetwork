package org.jnetwork;

import java.io.Serializable;

/**
 * A class used to represent the contents of a file.
 * 
 * @author Lucas Baizer
 */
public class FileData implements Serializable {
	private static final long serialVersionUID = 695740489657978707L;
	private byte[] data;

	/**
	 * Constructs a new <code>FileData</code> with the given data.
	 * 
	 * @param data
	 *            - An array of bytes which represents the contents of the file.
	 */
	public FileData(byte[] data) {
		this.data = data;
	}

	/**
	 * @return The bytecode of the file.
	 */
	public byte[] getData() {
		return data;
	}
}