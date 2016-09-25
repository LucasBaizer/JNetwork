package org.jnetwork.ui.database;

import java.io.Serializable;

public class Value<T> implements Serializable {
	private static final long serialVersionUID = -8691544073100922804L;

	private T value;

	public Value(T startValue) {
		this.value = startValue;
	}

	public T getValue() {
		return value;
	}

	public void setValue(T value) {
		this.value = value;
	}
}