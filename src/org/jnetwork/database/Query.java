package org.jnetwork.database;

import java.io.Serializable;
import java.util.ArrayList;

import org.jnetwork.database.DependencyQuery.HeaderDependency;

public abstract class Query implements Serializable {
	private static final long serialVersionUID = 4228446743268421639L;
	public static final int ACTION_ADD = 0;
	public static final int ACTION_REMOVE = 1;
	public static final int ACTION_GET = 2;
	public static final int ACTION_DROP = 3;
	public static final int ACTION_SET = 4;

	private int action;
	private String[] data;
	private String tableTarget;
	private String idTarget;
	private long queryTime = System.currentTimeMillis();

	public static Query parseQuery(String rawQuery) throws QueryException {
		String request = rawQuery = rawQuery.trim();
		String action = rawQuery.contains(" ") ? rawQuery.substring(0, rawQuery.indexOf(' ')).toLowerCase()
				: rawQuery.toLowerCase();

		Query query;
		if (action.equals("add")) {
			query = new AddQuery();

			setTable(query, request);
			if (!(request.contains("[") && request.contains("]"))) {
				throw new QueryException("ADD query must have column data in brackets");
			}

			String entryData = request.substring(request.indexOf('[') + 1, request.indexOf(']'));
			ArrayList<String> entries = new ArrayList<>();
			for (String data : entryData.split(",")) {
				entries.add(data.trim());
			}
			query.setData(entries.toArray(new String[entries.size()]));
		} else if (action.equals("remove")) {
			query = new RemoveQuery();

			setTable(query, request);

			if (request.toLowerCase().contains("entry")) {
				String idStart = request.substring(request.toLowerCase().indexOf("entry") + 6);
				String id = idStart.substring(0, idStart.indexOf(' '));
				if (id.toLowerCase().equals("in")) {
					throw new QueryException("REMOVE ENTRY query must have target entry ID");
				}

				query.setIDTarget(id);
			} else if (request.toLowerCase().contains("where ")) {
				parseDependencies((RemoveQuery) query, request);
			} else {
				throw new QueryException("REMOVE query must have a target entry");
			}
		} else if (action.equals("get")) {
			query = new GetQuery();

			setTable(query, request);

			if (request.toLowerCase().contains(" entry ")) {
				String idStart = request.substring(request.toLowerCase().indexOf("entry") + 6);
				String id = idStart.substring(0, idStart.indexOf(' '));
				if (id.toLowerCase().equals("in")) {
					throw new QueryException("GET ENTRY query must have target entry ID");
				}

				query.setIDTarget(id);
			} else {
				parseDependencies((GetQuery) query, request);
			}
		} else if (action.equals("drop")) {
			query = new DropQuery();
			String[] bits = request.split(" ");
			if (bits.length != 2) {
				throw new QueryException("DROP query must only have two elements (e.g. \"DROP MyTable\")");
			}

			query.setTableTarget(bits[1]);
		} else if (action.equals("set")) {
			query = new SetQuery();

			setTable(query, request);
			if (request.toLowerCase().contains(" entry ")) {
				String idStart = request.substring(request.toLowerCase().indexOf("entry") + 6);
				String id = idStart.substring(0, idStart.indexOf(' '));
				if (id.toLowerCase().equals("in")) {
					throw new QueryException("SET ENTRY query must have target entry ID");
				}

				query.setIDTarget(id);
			} else if (request.toLowerCase().contains("where ")) {
				parseDependencies((SetQuery) query, request);
			} else {
				throw new QueryException("SET query must have a target entry");
			}

			if (request.contains("[") && request.contains("]")) {
				if (!request.toLowerCase().contains(" to [")) {
					throw new QueryException("SET WHERE query must have TO element");
				}

				String entryData = request.substring(request.indexOf('[') + 1, request.indexOf(']'));
				ArrayList<String> entries = new ArrayList<>();
				for (String data : entryData.split(",")) {
					data = data.replaceAll("\"", "").replaceAll("'", "").trim();
					entries.add(data);
				}
				query.setData(entries.toArray(new String[entries.size()]));
			} else {
				throw new QueryException("SET query must have new data to set to");
			}
		} else {
			throw new QueryException("Invalid action: " + action);
		}
		return query;
	}

	private static void parseDependencies(DependencyQuery query, String request) throws QueryException {
		ArrayList<HeaderDependency> headerDependencies = new ArrayList<>();
		if (request.toLowerCase().contains(" where ")) {
			if (query instanceof SetQuery) {
				request = request.substring(0, request.toLowerCase().indexOf("to") - 1) + " IN "
						+ query.getTableTarget();
			}
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
					String columnValue = columnData.substring(columnData.indexOf(' ', columnData.indexOf(' ') + 1) + 1);

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
	}

	private static void setTable(Query query, String request) throws QueryException {
		String[] split = request.toLowerCase().split(" ");
		if (!split[split.length - 2].equals("in")) {
			throw new QueryException("Query must have a table target (e.g. 'IN MyTable')");
		}

		query.setTableTarget(request.substring(request.lastIndexOf(' ') + 1, request.length()));
	}

	public Query(String tableTarget, int action) {
		this.tableTarget = tableTarget;
		this.action = action;
	}

	public Query() {
		this(null, -1);
	}

	public int getAction() {
		return action;
	}

	public void setAction(int action) {
		this.action = action;
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

	public String getIDTarget() {
		return idTarget;
	}

	public void setIDTarget(String idTarget) {
		this.idTarget = idTarget;
	}
}
