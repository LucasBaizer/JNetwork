package org.jnetwork;

import java.io.Serializable;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Objects;

/**
 * Used to package {@link java.io.Serializable} objects together into a single
 * object to make code cleaner and easier to read, along with making sending and
 * receiving objects simpler. A message can also be added to the
 * <code>DataPackage</code> object so the receiver can logically work with any
 * received <code>DataPackage</code> in a simple, easy-to-use manner.
 * 
 * @author Lucas Baizer
 */
public class DataPackage implements Serializable {
	private static final long serialVersionUID = -4084673611320301450L;
	private String message = null;
	private Serializable[] objects;
	private String id = new BigInteger(130, new SecureRandom()).toString(32);

	/**
	 * Constructs a new DataPackage with all the needed data.
	 * 
	 * @param objects
	 *            - The required objects.
	 */
	public DataPackage(Serializable... objects) {
		this.objects = objects;
	}

	/**
	 * Adds a message to the <code>DataPackage</code> object to be saved in the
	 * <b> <code>message</code></b> variable so a client or server can read what
	 * the use of the <code>DataPackage</code> is.
	 * 
	 * @param message
	 *            - The String to set the message as.
	 * @return DataPackage - An instance of <code><b>this</b><code>.
	 */
	public DataPackage setMessage(String message) {
		this.message = message;
		return this;
	}

	/**
	 * Gets the set message.
	 * 
	 * @return String - The message.<br>
	 *         <code>null</code> - If there is no message.
	 */
	public String getMessage() {
		return this.message;
	}

	/**
	 * Gets if the <code>DataPackage</code> has a message attached.
	 * 
	 * @return boolean - If there is a message.
	 */
	public boolean hasMessage() {
		return !Objects.equals(message, null);
	}

	/**
	 * Gets all of the objects added when the constructor was called.S
	 * 
	 * @return Object[] - The objects.
	 */
	public Serializable[] getObjects() {
		return objects;
	}

	/**
	 * @return A random alphanumeric ID created at the time of this
	 *         DataPackage's instantiation.
	 */
	public String getID() {
		return id;
	}

	/**
	 * Gets a String representation of the class in the form of the message.
	 */
	@Override
	public String toString() {
		return "DataPackage:[message=" + message + ",id=" + id + ",objects=" + Arrays.asList(objects) + "]";
	}

	/**
	 * Gets if this DataPackage is equal to another.
	 * 
	 * @return <code>true</code> if the other object is of type DataPackage and
	 *         has the same message as this DataPackage, as well as if the other
	 *         object's contents are equal to this ones' (as defined by
	 *         {@link Arrays#equals(Object[], Object[])}.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof DataPackage) {
			DataPackage op = (DataPackage) o;
			return op.getMessage().equals(this.getMessage()) && Arrays.equals(op.getObjects(), this.getObjects())
					&& op.getID().equals(this.getID());
		}
		return false;
	}
}