package org.jnetwork;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * A utility class utilized by <code>Connection</code> to make the transfer of
 * objects easier.
 *
 * @see org.jnetwork.Connection
 * @see java.io.ObjectOutputStream
 *
 * @author Lucas Baizer
 */
public class AdvancedOutputStream extends ObjectOutputStream {
	/**
	 * Instantiates a new AdvancedOutputStream.
	 * 
	 * @param out
	 *            - The OutputStream to create off of.
	 * 
	 * @throws IOException
	 *             If a general I/O error occurs.
	 * 
	 * @see java.io.ObjectOutputStream#ObjectOutputStream(OutputStream)
	 */
	public AdvancedOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	/**
	 * Writes out a file in the format of a {@link org.jnetwork.FileData}
	 * object.
	 * 
	 * @param file
	 *            - The File to write out.
	 * @throws IOException
	 *             - If an I/O error occurs.
	 */
	public void writeFile(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getName());
		}

		byte[] bytes = new byte[(int) file.length()];
		FileInputStream in = new FileInputStream(file);
		in.read(bytes);
		in.close();

		writeObject(new FileData(bytes));
	}
}