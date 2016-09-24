package org.jnetwork;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;

/**
 * A utility class used for closing unclosed <code>Closeable</code> objects on
 * shutdown.
 * 
 * This class is immutable.
 * 
 * @author Lucas Baizer
 */
public final class CloseRequest {
	private static final ArrayList<Closeable> toClose = new ArrayList<Closeable>();

	static {
		Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
			@Override
			public void run() {
				for (Closeable object : toClose) {
					try {
						object.close();
					} catch (IOException e) {
						Thread.getDefaultUncaughtExceptionHandler().uncaughtException(Thread.currentThread(), e);
					}
				}
			}
		}));
	}

	/**
	 * Adds a <code>Closeable</code> object to close at runtime.
	 * 
	 * @param obj
	 *            - The <code>Closeable</code> object to close at runtime.
	 * 
	 * @return boolean - If the adding was successful. Defined by
	 *         {@link java.util.Collection#add(Object)}.
	 * 
	 * @throws NullPointerException
	 *             If <code>obj</code> is <code>null</code>.
	 * 
	 * @see java.util.Collection#add(Object)
	 */
	public static boolean addObjectToClose(Closeable obj) {
		if (obj == null)
			throw new NullPointerException();

		return toClose.add(obj);
	}

	/**
	 * Removes a previously added <code>Closeable</code> object to close at
	 * runtime.
	 * 
	 * @param obj
	 *            - The <code>Closeable</code> object to remove.
	 * 
	 * @return boolean - If the removing was successful. Defined by
	 *         {@link java.util.Collection#remove(Object)}.
	 * 
	 * @throws NullPointerException
	 *             If <code>obj</code> is <code>null</code>.
	 * 
	 * @see java.util.Collection#remove(Object)
	 */
	public static boolean removeObjectToClose(Closeable obj) {
		if (obj == null)
			throw new NullPointerException();

		return toClose.remove(obj);
	}

	/**
	 * Gets all of the <code>Closeable</code> objects that will be closed at
	 * shutdown.
	 * 
	 * @return Closeable[] - The array of objects to close.
	 */
	public static Closeable[] getObjectsToClose() {
		Closeable[] objects = new Closeable[toClose.size()];

		for (int i = 0; i < objects.length; i++)
			objects[i] = toClose.get(i);

		return objects;
	}
}