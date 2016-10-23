package org.jnetwork;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * A collection of some utilities that can be found useful when using UDP
 * sockets.
 * 
 * @author Lucas Baizer
 */
public final class UDPUtils {
	private UDPUtils() {
	}

	/**
	 * Serializes an object into its byte format.
	 * 
	 * @param obj
	 *            - The object to serialize.
	 * @return The serialized bytes.
	 * @throws IOException
	 *             If there is an error with writing the object through the
	 *             stream.
	 */
	public static byte[] serializeObject(Serializable obj) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			try (ObjectOutputStream w = new ObjectOutputStream(out)) {
				w.writeObject(obj);
			}
			return out.toByteArray();
		}
	}

	/**
	 * Deserializes an object from its byte format.
	 * 
	 * @param obj
	 *            - The serialized format to deserialize.
	 * @return The deserialized object.
	 * @throws IOException
	 *             If an error occurs with reading the object from the stream.
	 * @throws EOFException
	 *             If an object is serialized and written through a stream, but
	 *             the array is cut off. When this object is deserialized, there
	 *             is no definite end to the object, which causes an EOF.
	 * @throws ClassNotFoundException
	 *             If the serialized object's class cannot be found.
	 */
	public static <T extends Serializable> T deserializeObject(byte[] obj) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(obj)) {
			try (ObjectInputStream r = new ObjectInputStream(in)) {
				return (T) r.readObject();
			}
		}
	}
}
