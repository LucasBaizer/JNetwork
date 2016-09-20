package org.jnetwork.database;

import java.io.Serializable;

public class ColumnHeader implements Serializable {
	private static final long serialVersionUID = 3543585744208754423L;
	public static final int STORAGE_TYPE_INTEGER = 0;
	public static final int STORAGE_TYPE_DECIMAL = 1;
	public static final int STORAGE_TYPE_STRING = 2;

	private String columnName;
	private int storageType;

	public ColumnHeader(String columnName, int storageType) {
		this.setColumnName(columnName);
		this.setStorageType(storageType);
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	public int getStorageType() {
		return storageType;
	}

	public void setStorageType(int storageType) {
		this.storageType = storageType;
	}

	@Override
	public String toString() {
		return columnName;
	}
}
