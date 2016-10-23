package org.jnetwork;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.Objects;

/**
 * A utility class utilized by <code>Connection</code> to make the tranfer of
 * objects easier.
 *
 * @see org.jnetwork.Connection
 * @see java.io.ObjectInputStream
 *
 * @author Lucas Baizer
 */
public class AdvancedInputStream extends ObjectInputStream {
	/**
	 * Instantiates a new AdvancedInputStream.
	 * 
	 * @param in
	 *            - The InputStream to create off of.
	 * 
	 * @throws IOException
	 *             If a general I/O error occurs.
	 * 
	 * @see java.io.ObjectInputStream#ObjectInputStream(InputStream)
	 */
	public AdvancedInputStream(InputStream in) throws IOException {
		super(in);
	}

	/**
	 * Reads in a file's bytecode and outputs it to a file.
	 * 
	 * @param outputFile
	 *            - The file to output the bytecode to.
	 * 
	 * @throws IOException
	 *             - If an I/O error occurs.
	 * @throws ClassNotFoundException
	 *             See {@link #readSpecificType(Class)}.
	 */
	public void readFile(File outputFile) throws ClassNotFoundException, IOException {
		if (!outputFile.exists()) {
			throw new FileNotFoundException(outputFile.getAbsolutePath());
		}

		byte[] data = ((FileData) readSpecificType(FileData.class)).getData();

		FileOutputStream fos = new FileOutputStream(outputFile);
		fos.write(data);
		fos.close();
	}

	/**
	 * Will read in objects from the server continuously until one is received
	 * of the same class as the {@code obj} parameter.
	 * 
	 * @param obj
	 *            - The class that the read Object must be.
	 * 
	 * @return Object - An object of the same type of the {@code obj} parameter.
	 *
	 * 
	 * @throws ClassNotFoundException
	 *             If the server sends a type which does not exist on the client
	 *             machine.
	 * 
	 * @throws IOException
	 *             If there is an error reading the Object.
	 * @throws NullPointerException
	 *             If <code>obj</code> is <code>null</code>.
	 **/
	public <T> T readSpecificType(Class<? extends Serializable> obj) throws ClassNotFoundException, IOException {
		Object objIn;

		while (true) {
			objIn = readObject();

			if (objIn != null) {
				if (objIn.getClass() == obj) {
					return (T) objIn;
				}
			} else {
				if (obj == null)
					return null;
			}
		}
	}

	/**
	 * Will read in objects from the server {@code repeats} times until one if
	 * received of the same type as the {@code obj} parameter. Will return
	 * {@code null} if no object of the same type as the {@code obj} parameter
	 * is returned after {@code repeats} retries.
	 * 
	 * @param obj
	 *            - The class the the read Object must be.
	 * 
	 * @param repeats
	 *            - The amount of times to read in an Object and test if it is
	 *            the correct type.
	 * 
	 * @return If an Object of {@code obj} type is received, it will return the
	 *         received Object.
	 * 
	 *         If no Object of {@code obj} type is received after
	 *         {@code repeats} retries, {@code null} is returned.
	 * 
	 * @throws ClassNotFoundException
	 *             If the server sends a type which does not exist on the client
	 *             machine.
	 * @throws IOException
	 *             If there is a failure sending/receiving an Object.@throws
	 *             NullPointerException If <code>obj</code> is <code>null</code>
	 *             .
	 **/
	public <T> T readSpecificType(Class<? extends Serializable> obj, int repeats)
			throws ClassNotFoundException, IOException {
		Object objIn;

		if (repeats < 1)
			throw new NumberFormatException("Repeats is less than 1: " + repeats);

		for (int i = 0; i < repeats; i++) {
			objIn = readObject();

			if (objIn != null) {
				if (objIn.getClass() == obj) {
					return (T) objIn;
				}
			} else {
				if (obj == null)
					return null;
			}
		}
		return null;
	}

	/**
	 * Will read in objects from the server continuously until one is received
	 * and and array.
	 * 
	 * @return Object - The read array object.
	 * 
	 * @throws ClassNotFoundException
	 *             If the server sends a type which does not exist on the client
	 *             machine.
	 * 
	 * @throws IOException
	 *             If there is an error reading the Object.
	 */
	public <T> T[] readObjectArray() throws IOException, ClassNotFoundException {
		Object objIn;

		while (true) {
			objIn = readObject();

			if (objIn != null) {
				if (objIn.getClass().isArray())
					break;
			}
		}
		return (T[]) objIn;
	}

	/**
	 * Will read in objects from the server {@code repeats} times until one if
	 * received that is an array. Will return {@code null} if no array of the is
	 * returned after {@code repeats} retries.
	 * 
	 * @param repeats
	 *            - The amount of times to read in an Object and test if it is
	 *            the correct type.
	 * 
	 * @return If an array is received, it will return the received Object.
	 * 
	 *         If no array is received after {@code repeats} retries,
	 *         {@code null} is returned.
	 * 
	 * @throws ClassNotFoundException
	 *             If the server sends a type which does not exist on the client
	 *             machine.
	 * @throws IOException
	 *             If there is a failure sending/receiving an Object.
	 **/
	public <T> T[] readObjectArray(int retries) throws IOException, ClassNotFoundException {
		Object objIn;

		for (int i = 0; i < retries; i++) {
			objIn = readObject();

			if (objIn != null) {
				if (objIn.getClass().isArray())
					return (T[]) objIn;
			}
		}
		return null;
	}

	/**
	 * Will read in objects from the server continuously until one is received
	 * of the same class as the {@code obj} parameter and is an array.
	 * 
	 * @param type
	 *            - The class that the read Object must be.
	 * 
	 * @return Object - An object of the same type of the {@code obj} parameter.
	 *
	 * 
	 * @throws ClassNotFoundException
	 *             If the server sends a type which does not exist on the client
	 *             machine.
	 * 
	 * @throws IOException
	 *             If there is an error reading the Object.
	 * @throws NullPointerException
	 *             If <code>type</code> is <code>null</code>.
	 **/
	public <T> T[] readSpecificTypeArray(Class<? extends Serializable> type)
			throws ClassNotFoundException, IOException {
		if (Objects.equals(type, null))
			throw new NullPointerException();

		if (!type.isArray())
			throw new ArrayStoreException("Class is not an array");
		Object objIn;

		while (true) {
			objIn = readObject();

			if (objIn != null) {
				if (objIn.getClass().isArray()) {
					if (objIn.getClass() == type)
						break;
				}
			} else {
				if (type == null)
					return null;
			}
		}
		return (T[]) objIn;
	}

	/**
	 * Will read in objects from the server {@code repeats} times until one if
	 * received of the same type as the {@code obj} parameter and is an array.
	 * Will return {@code null} if no object of the same type as the {@code obj}
	 * parameter and is an array is returned after {@code repeats} retries.
	 * 
	 * @param obj
	 *            - The class the the read Object must be.
	 * 
	 * @param repeats
	 *            - The amount of times to read in an Object and test if it is
	 *            the correct type and an array.
	 * 
	 * @return If an Object of {@code obj} type is received, it will return the
	 *         received Object.
	 * 
	 *         If no Object of {@code obj} type is received after
	 *         {@code repeats} retries, {@code null} is returned.
	 * 
	 * @throws ClassNotFoundException
	 *             If the server sends a type which does not exist on the client
	 *             machine.
	 * @throws IOException
	 *             If there is a failure sending/receiving an Object.
	 * @throws ArrayStoreException
	 *             If <code>type</code> is not an array type.
	 * @throws NullPointerException
	 *             If <code>type</code> is <code>null</code>.
	 **/
	public <T> T[] readSpecificTypeArray(Class<? extends Serializable> type, int retries)
			throws ClassNotFoundException, IOException {
		if (!type.isArray())
			throw new ArrayStoreException("Class is not an array");
		Object objIn;

		for (int i = 0; i < retries; i++) {
			objIn = readObject();

			if (objIn != null) {
				if (objIn.getClass().isArray()) {
					if (objIn.getClass() == type)
						return (T[]) objIn;
				}
			} else {
				if (type == null)
					return null;
			}
		}
		return null;
	}
}