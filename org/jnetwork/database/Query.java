package org.jnetwork.database;

import java.io.Serializable;
import java.util.ArrayList;

public class Query implements Serializable {
	private static final long serialVersionUID = 4228446743268421639L;
	public static final int ACTION_ADD = 0;
	public static final int ACTION_REMOVE = 1;
	public static final int ACTION_GET = 2;
	public static final int ACTION_DROP = 3;
	public static final int ACTION_SET = 4;

	private int action;
	private HeaderDependency[] headerDependencies;
	private String[] data;
	private String tableTarget;
	private long queryTime = System.currentTimeMillis();

	public static Query parseQuery(String request) {
		request = request.trim();
		String action = request.substring(0, request.indexOf(' ')).toLowerCase();

		Query query = new Query();
		if (action.equals("add")) {
			query.setAction(Query.ACTION_ADD);
			setTable(query, request);
			if (!(request.contains("[") && request.contains("]"))) {
				throw new QueryException("ADD query must have column data in brackets");
			}

			query.setTableTarget(request.substring(request.lastIndexOf(' ') + 1, request.length()));

			String entryData = request.substring(request.indexOf('[') + 1, request.indexOf(']'));
			ArrayList<String> entries = new ArrayList<>();
			for (String data : entryData.split(",")) {
				data = data.replaceAll("\"", "").replaceAll("'", "").trim();
				entries.add(data);
			}
			query.setData(entries.toArray(new String[entries.size()]));
		} else if (action.equals("get")) {
			query.setAction(Query.ACTION_GET);
			setTable(query, request);

			ArrayList<HeaderDependency> headerDependencies = new ArrayList<>();
			if (request.toLowerCase().contains("where ")) {
				try {
					for (String columnData : request.substring(request.toLowerCase().indexOf("where") + 6,
							request.toLowerCase().lastIndexOf("in") - 1).split("(?i)and")) {
						columnData = columnData.trim();

						String columnName;
						try {
							columnName = columnData.substring(0, columnData.indexOf(' '));
						} catch (StringIndexOutOfBoundsException e) {
							throw new QueryException("No comparator specified");
						}
						String columnValue = columnData
								.substring(columnData.indexOf(' ', columnData.indexOf(' ') + 1) + 1);

						String oComparator = columnData.substring(columnData.indexOf(' ') + 1,
								columnData.indexOf(' ', columnData.indexOf(' ') + 1));
						String comparator = oComparator.toLowerCase();

						int compare = -1;
						if (comparator.equals("is")) {
							compare = HeaderDependency.ACTION_EQUALS;
						} else if (comparator.equals("not")) {
							compare = HeaderDependency.ACTION_NOT_EQUALS;
						} else if (comparator.equals("includes")) {
							compare = HeaderDependency.ACTION_CONTAINS;
						} else if (comparator.equals("excludes")) {
							compare = HeaderDependency.ACTION_NOT_CONTAINS;
						} else {
							throw new QueryException("Invalid comparator: " + oComparator);
						}
						headerDependencies.add(new HeaderDependency(columnName, columnValue, compare));
					}
				} catch (StringIndexOutOfBoundsException e) {
					throw new QueryException("Nothing specified to compare in WHERE statement");
				}
			}
			query.setHeaderDependencies(headerDependencies.toArray(new HeaderDependency[headerDependencies.size()]));
		} else {
			throw new QueryException("Invalid action: " + action);
		}
		return query;
	}

	private static void setTable(Query query, String request) {
		String[] split = request.toLowerCase().split(" ");
		if (!split[split.length - 2].equals("in")) {
			throw new QueryException("Query must have a table target (e.g. 'IN MyTable')");
		}

		query.setTableTarget(request.substring(request.lastIndexOf(' ') + 1, request.length()));
	}

	public Query(String tableTarget, int action, HeaderDependency[] dependencies) {
		this.tableTarget = tableTarget;
		this.action = action;
		this.headerDependencies = dependencies;
	}

	public Query() {
		this(null, -1, null);
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public HeaderDependency[] getHeaderDependencies() {
		return headerDependencies;
	}

	public void setHeaderDependencies(HeaderDependency[] headerDependencies) {
		this.headerDependencies = headerDependencies;
	}

	public boolean hasHeaderDependencies() {
		return headerDependencies != null && headerDependencies.length > 0;
	}

	public String[] getData() {
		return data;
	}

	public void setData(String[] data) {
		this.data = data;
	}

	public String getTableTarget() {
		return tableTarget;
	}

	public void setTableTarget(String tableTarget) {
		this.tableTarget = tableTarget;
	}

	long getQueryTime() {
		return queryTime;
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
