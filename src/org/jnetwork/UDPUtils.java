package org.jnetwork;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class UDPUtils {
	private UDPUtils() {
	}

	public static byte[] serializeObject(Serializable obj) throws IOException {
		try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
			try (ObjectOutputStream w = new ObjectOutputStream(out)) {
				w.writeObject(obj);
			}
			return out.toByteArray();
		}
	}

	public static <T extends Serializable> T deserializeObject(byte[] obj) throws IOException, ClassNotFoundException {
		try (ByteArrayInputStream in = new ByteArrayInputStream(obj)) {
			try (ObjectInputStream r = new ObjectInputStream(in)) {
				return (T) r.readObject();
			}
		}
	}
}
