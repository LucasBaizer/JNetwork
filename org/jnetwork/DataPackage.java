package org.jnetwork;

import java.io.Serializable;
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
public class DataPackage implements Serializable, Cloneable {
	private static final long serialVersionUID = -4084673611320301450L;
	private String message = null;
	private Serializable[] objects;

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
	public Object[] getObjects() {
		return objects;
	}

	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}

	/**
	 * Gets a String representation of the class in the form of the message. If
	 * no message is set, <code>Object.toString()</code> is returned.
	 */
	@Override
	public String toString() {
		return hasMessage() ? getMessage() : super.toString();
	}
}