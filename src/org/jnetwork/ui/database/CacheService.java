package org.jnetwork.ui.database;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

@SuppressWarnings("unchecked")
public class CacheService<T> {
	public T loadCache(File file, T defaultObj) {
		try {
			if (file.exists()) {
				try (FileInputStream fin = new FileInputStream(file)) {
					try (ObjectInputStream oin = new ObjectInputStream(fin)) {
						return (T) oin.readObject();
					}
				}
			} else {
				file.createNewFile();
				saveCache(file, defaultObj);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public void saveCache(File file, T obj) {
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			try (FileOutputStream fout = new FileOutputStream(file)) {
				try (ObjectOutputStream oout = new ObjectOutputStream(fout)) {
					oout.writeObject(obj);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
