package org.jnetwork.database;

import java.io.Serializable;

public class DependencyQuery extends Query {
	private static final long serialVersionUID = -2222103824173486874L;

	private HeaderDependency[] headerDependencies;

	public HeaderDependency[] getHeaderDependencies() {
		return headerDependencies;
	}

	public void setHeaderDependencies(HeaderDependency[] headerDependencies) {
		this.headerDependencies = headerDependencies;
	}

	public boolean hasHeaderDependencies() {
		return headerDependencies != null && headerDependencies.length > 0;
	}

	public static class HeaderDependency implements Serializable {
		private static final long serialVersionUID = -6599166644090673912L;
		public static final int ACTION_EQUALS = 0;
		public static final int ACTION_NOT_EQUALS = 1;
		public static final int ACTION_CONTAINS = 2;
		public static final int ACTION_NOT_CONTAINS = 3;

		private int action;
		private String header;
		private Serializable value;

		public HeaderDependency(String header, Serializable value, int action) {
			this.action = action;
			this.header = header;
			this.value = value;
		}

		public int getAction() {
			return action;
		}

		public void setAction(int action) {
			this.action = action;
		}

		public String getHeader() {
			return header;
		}

		public void setHeader(String header) {
			this.header = header;
		}

		public Serializable getValue() {
			return value;
		}

		public void setValue(Serializable value) {
			this.value = value;
		}
	}
}
