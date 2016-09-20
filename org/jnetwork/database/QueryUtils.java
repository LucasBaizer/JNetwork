package org.jnetwork.database;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;

public final class QueryUtils {
	private QueryUtils() {
	}

	public static String getSerialized(Serializable o) throws IOException {
		try (ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
			try (ObjectOutputStream oout = new ObjectOutputStream(bout)) {
				oout.writeObject(o);
				return new String(bout.toByteArray(), "UTF-8");
			}
		}
	}
}
