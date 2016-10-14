package org.jnetwork.database;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnetwork.database.DependencyQuery.HeaderDependency;

public class Table implements Serializable {
	private static final long serialVersionUID = 7406644564523364391L;

	private String name;
	private ColumnHeader[] columnHeaders;
	private File tableFile;
	private transient PrintStream out;
	private boolean dropped = false;

	public static Table load(File file) throws IOException {
		if (!file.exists()) {
			throw new FileNotFoundException(file.getPath());
		}

		String raw = new String(Files.readAllBytes(file.toPath()), "UTF-8");
		String tableData = "";
		Matcher matcher = Pattern.compile("\\[(.+?)\\]").matcher(raw);
		while (matcher.find()) {
			tableData = matcher.group(1);
		}

		String[] parsed = tableData.split(",");
		String tableName = parsed[0];

		ArrayList<ColumnHeader> headers = new ArrayList<>();
		for (int i = 1; i < parsed.length; i++) {
			String column = parsed[i];
			String[] data = column.split("\\:");
			headers.add(new ColumnHeader(data[1], Integer.parseInt(data[0])));
		}

		Table table = new Table(file.getParent(), tableName, headers.toArray(new ColumnHeader[headers.size()]));
		return table;
	}

	public Table(String parentFolder, String name, ColumnHeader[] columnHeaders) throws IOException {
		this.name = name;
		this.columnHeaders = columnHeaders;
		this.tableFile = new File(parentFolder + File.separator + name + ".table");

		if (!tableFile.exists()) {
			tableFile.createNewFile();

			this.out = new PrintStream(new FileOutputStream(tableFile, true));
			out.print("[" + name);
			for (ColumnHeader header : columnHeaders) {
				out.print("," + header.getStorageType() + ":" + header.getColumnName());
			}
			out.print("]");
		} else {
			this.out = new PrintStream(new FileOutputStream(tableFile, true));
		}
	}

	public ColumnHeader[] getColumnHeaders() {
		return columnHeaders;
	}

	public void setColumnHeaders(ColumnHeader[] columnHeaders) {
		this.columnHeaders = columnHeaders;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	EntrySet query(String request) throws IOException, QueryException {
		return query(Query.parseQuery(request));
	}

	EntrySet query(Query request) throws IOException, QueryException {
		checkDropped();

		if (request.getAction() == Query.ACTION_ADD) {
			String objectToWrite = constructRaw(request);
			writeEntry("{" + objectToWrite + "}");

			return new EntrySet(asEntry(objectToWrite, null));
		} else if (request.getAction() == Query.ACTION_REMOVE) {
			String table = readTable();

			if (request.getIDTarget() != null) {
				EntryPoint p = getEntryByID(request.getIDTarget(), table);

				StringBuffer buffer = new StringBuffer(table);
				buffer.replace(p.getStartIndex(), p.getEndIndex(), "");

				Files.write(tableFile.toPath(), buffer.toString().getBytes());

				return new EntrySet(p.getEntry());
			} else {
				StringBuffer buffer = new StringBuffer(table);

				EntrySet set = new EntrySet();
				checkEntries(request, new CriteriaCallback() {
					@Override
					public void criteriaMet(Entry entry) {
						String buf = buffer.toString();
						int look = buf.indexOf(entry.getEntryID()) - 1;
						int stop = look + buf.substring(look, table.indexOf('}', look)).length() + 1;
						buffer.replace(look, stop, "");

						set.addEntry(entry);
					}
				}, table);

				Files.write(tableFile.toPath(), buffer.toString().getBytes());
				return set;
			}
		} else if (request.getAction() == Query.ACTION_GET) {
			EntrySet data = new EntrySet();
			if (request.getIDTarget() == null) {
				checkEntries(request, new CriteriaCallback() {
					@Override
					public void criteriaMet(Entry entry) {
						data.addEntry(entry);
						entry.setQueryTime(System.currentTimeMillis() - request.getQueryTime());
					}
				}, readTable());
			} else {
				data.addEntry(getEntryByID(request.getIDTarget(), readTable()).getEntry());
			}
			return data;
		} else if (request.getAction() == Query.ACTION_DROP) {
			this.tableFile.delete();
			dropped = true;
		} else if (request.getAction() == Query.ACTION_SET) {
			String table = readTable();

			if (request.getIDTarget() != null) {
				EntryPoint entry = getEntryByID(request.getIDTarget(), table);

				StringBuffer buf = new StringBuffer(table);
				buf.replace(entry.getStartIndex(), entry.getEndIndex(), "");
				buf.insert(entry.getStartIndex(),
						"{" + constructRawSet(request, entry.getEntry(), request.getIDTarget()) + "}");

				Files.write(tableFile.toPath(), buf.toString().getBytes());
				return new EntrySet(entry.getEntry());
			} else {
				StringBuffer buffer = new StringBuffer(table);

				EntrySet set = new EntrySet();
				checkEntries(request, new CriteriaCallback() {
					@Override
					public void criteriaMet(Entry entry) throws QueryException {
						String buf = buffer.toString();
						int look = buf.indexOf(entry.getEntryID()) - 1;
						int stop = look + buf.substring(look, table.indexOf('}', look)).length() + 1;
						String construct = constructRawSet(request, entry, entry.getEntryID());

						buffer.replace(look, stop, "");
						buffer.insert(look, "{" + construct + "}");

						set.addEntry(entry);
					}
				}, table);

				Files.write(tableFile.toPath(), buffer.toString().getBytes());
				return set;
			}
		}
		return null;
	}

	private String constructRaw(Query request) throws QueryException {
		return constructRaw(request, new BigInteger(64, new SecureRandom()).toString(16));
	}

	private String constructRawSet(Query request, Entry original, String id) throws QueryException {
		if (request.getData().length != columnHeaders.length) {
			throw new QueryException("Request data array length inequal to column headers array length");
		}

		int i = 0;
		String objectToWrite = id + ";";
		for (String obj : request.getData()) {
			if (!obj.equals("*")) {
				if (isInteger(obj)) {
					if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_INTEGER) {
						System.out.println();
						throw new QueryException("Object '" + obj + "' does not follow "
								+ columnHeaders[i].getColumnName() + " storage type: integer");
					}
				} else if (isDecimal(obj)) {
					if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_DECIMAL) {
						throw new QueryException("Object '" + obj + "' does not follow "
								+ columnHeaders[i].getColumnName() + " storage type: decimal");
					}
				} else {
					if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_STRING) {
						throw new QueryException("Object '" + obj + "' does not follow column "
								+ columnHeaders[i].getColumnName() + " storage type: string");
					}
				}
				objectToWrite += obj + ",";
			} else {
				objectToWrite += original.getData()[i].toString() + ",";
			}
			i++;
		}

		// substring is to remove comma at the end
		return objectToWrite.substring(0, objectToWrite.length() - 1);
	}

	private String constructRaw(Query request, String id) throws QueryException {
		if (request.getData().length != columnHeaders.length) {
			throw new QueryException("Request data array length inequal to column headers array length");
		}

		int i = 0;
		String objectToWrite = id + ";";
		for (String obj : request.getData()) {
			if (isInteger(obj)) {
				if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_INTEGER) {
					throw new QueryException("Object '" + obj + "' does not follow " + columnHeaders[i].getColumnName()
							+ " storage type: integer");
				}
			} else if (isDecimal(obj)) {
				if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_DECIMAL) {
					throw new QueryException("Object '" + obj + "' does not follow " + columnHeaders[i].getColumnName()
							+ " storage type: decimal");
				}
			} else {
				if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_STRING) {
					throw new QueryException("Object '" + obj + "' does not follow column "
							+ columnHeaders[i].getColumnName() + " storage type: string");
				}
			}
			objectToWrite += obj.replaceAll("\"", "").replaceAll("'", "") + ",";
			i++;
		}

		// substring is to remove comma at the end
		return objectToWrite.substring(0, objectToWrite.length() - 1);
	}

	private EntryPoint getEntryByID(String targetID, String table) throws QueryException {
		Matcher matcher = Pattern.compile("\\{(.+?)\\}").matcher(table);

		int startIndex = -1;
		int endIndex = -1;

		while (matcher.find()) {
			String entry = matcher.group(1);

			String id = entry.substring(0, entry.indexOf(';'));
			if (id.equals(targetID)) {
				startIndex = table.indexOf(entry) - 1;
				endIndex = startIndex + entry.length() + 2;
				break;
			}
		}

		if (startIndex == -1 || endIndex == -1) {
			throw new QueryException("No entry with ID: " + targetID);
		}

		return new EntryPoint(asEntry(table.substring(startIndex + 1, endIndex - 1), null), startIndex, endIndex);
	}

	private void checkEntries(Query request, CriteriaCallback callback, String table)
			throws IOException, QueryException {
		Matcher matcher = Pattern.compile("\\{(.+?)\\}").matcher(table);

		for (HeaderDependency dependency : ((DependencyQuery) request).getHeaderDependencies()) {
			boolean exists = false;
			for (ColumnHeader header : columnHeaders) {
				if (dependency.getHeader().equals(header.getColumnName())) {
					exists = true;
					break;
				}
			}
			if (!exists) {
				throw new QueryException("Invalid header dependency: " + dependency.getHeader());
			}

			dependency.setValue(
					isInteger(dependency.getValue().toString()) ? Integer.parseInt(dependency.getValue().toString())
							: (isDecimal(dependency.getValue().toString())
									? Double.parseDouble(dependency.getValue().toString())
									: dependency.getValue().toString()));

			for (ColumnHeader header : columnHeaders) {
				if (dependency.getHeader().equals(header.getColumnName())) {
					if (dependency.getValue() instanceof Integer
							&& header.getStorageType() != ColumnHeader.STORAGE_TYPE_INTEGER) {
						throw new QueryException("Header dependency value " + dependency.getValue()
								+ " does not follow " + header.getColumnName() + " storage type: "
								+ getHeaderType(header.getStorageType()));
					} else if (dependency.getValue() instanceof Double
							&& header.getStorageType() != ColumnHeader.STORAGE_TYPE_DECIMAL) {
						throw new QueryException("Header dependency value " + dependency.getValue()
								+ " does not follow " + header.getColumnName() + " storage type: "
								+ getHeaderType(header.getStorageType()));
					} else if ((!(dependency.getValue() instanceof Integer)
							&& !(dependency.getValue() instanceof Double))
							&& header.getStorageType() != ColumnHeader.STORAGE_TYPE_STRING) {
						System.out.println(header.getStorageType());
						throw new QueryException("Header dependency value " + dependency.getValue()
								+ " does not follow " + header.getColumnName() + " storage type: "
								+ getHeaderType(header.getStorageType()));
					}
				}
			}
		}
		while (matcher.find()) {
			String raw = matcher.group(1);
			Entry e = asEntry(raw, ((DependencyQuery) request).getHeaderDependencies());
			if (e != null) {
				callback.criteriaMet(e);
			}
		}
	}

	private String getHeaderType(int type) {
		return type == ColumnHeader.STORAGE_TYPE_INTEGER ? "integer"
				: type == ColumnHeader.STORAGE_TYPE_DECIMAL ? "decimal" : "string";
	}

	private Entry asEntry(String raw, HeaderDependency[] dependencies) {
		String id = raw.substring(0, raw.indexOf(';'));
		String content = raw.substring(raw.indexOf(';') + 1, raw.length());

		Entry entry = new Entry(id);
		int i = 0;
		for (String element : content.split(",")) {
			String value = element;

			ColumnHeader col = columnHeaders[i];
			Serializable sValue = null;
			if (col.getStorageType() == ColumnHeader.STORAGE_TYPE_INTEGER) {
				entry.setData(col.getColumnName(), sValue = Integer.parseInt(value));
			} else if (col.getStorageType() == ColumnHeader.STORAGE_TYPE_DECIMAL) {
				entry.setData(col.getColumnName(), sValue = Double.parseDouble(value));
			} else if (col.getStorageType() == ColumnHeader.STORAGE_TYPE_STRING) {
				entry.setData(col.getColumnName(), sValue = value);
			}
			if (dependencies != null) {
				for (HeaderDependency dependency : dependencies) {
					if ((dependency.getValue().toString().startsWith("\"")
							&& dependency.getValue().toString().endsWith("\""))
							|| (dependency.getValue().toString().startsWith("'")
									&& dependency.getValue().toString().endsWith("'"))) {
						String dValue = dependency.getValue().toString().replaceAll("\"", "").replaceAll("'", "");
						if (dependency.getAction() == HeaderDependency.ACTION_EQUALS
								&& dependency.getHeader().equals(col.getColumnName()) && !dValue.equals(sValue)) {
							return null;
						} else if (dependency.getAction() == HeaderDependency.ACTION_NOT_EQUALS
								&& dependency.getHeader().equals(col.getColumnName()) && dValue.equals(sValue)) {
							return null;
						} else if (dependency.getAction() == HeaderDependency.ACTION_CONTAINS
								&& dependency.getHeader().equals(col.getColumnName())
								&& !sValue.toString().contains(dValue)) {
							return null;
						} else if (dependency.getAction() == HeaderDependency.ACTION_NOT_CONTAINS
								&& dependency.getHeader().equals(col.getColumnName())
								&& sValue.toString().contains(dValue)) {
							return null;
						}
					} else {
						if (dependency.getAction() == HeaderDependency.ACTION_EQUALS
								&& dependency.getHeader().equals(col.getColumnName())
								&& !dependency.getValue().equals(sValue)) {
							return null;
						} else if (dependency.getAction() == HeaderDependency.ACTION_NOT_EQUALS
								&& dependency.getHeader().equals(col.getColumnName())
								&& dependency.getValue().equals(sValue)) {
							return null;
						} else if (dependency.getAction() == HeaderDependency.ACTION_CONTAINS
								&& dependency.getHeader().equals(col.getColumnName())
								&& !sValue.toString().contains(dependency.getValue().toString())) {
							return null;
						} else if (dependency.getAction() == HeaderDependency.ACTION_NOT_CONTAINS
								&& dependency.getHeader().equals(col.getColumnName())
								&& sValue.toString().contains(dependency.getValue().toString())) {
							return null;
						}
					}
				}
			}
			i++;
		}
		return entry;
	}

	private String readTable() throws IOException {
		return new String(Files.readAllBytes(tableFile.toPath()), "UTF-8");
	}

	private void writeEntry(String entry) {
		out.print(entry);
	}

	private boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				return false;
			}
		}
		return true;
	}

	private boolean isDecimal(String str) {
		if (str == null) {
			return false;
		}
		if ((str.startsWith("\"") && str.endsWith("\"")) || (str.startsWith("'") && str.endsWith("'"))) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		int d = 0;
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c < '0' || c > '9') {
				if (c == '.') {
					if (d == 1) {
						return false;
					}
					d++;
				} else {
					return false;
				}
			}
		}
		return true;
	}

	private void checkDropped() throws QueryException {
		if (dropped)
			throw new QueryException("Cannot modify a dropped table");
	}

	public File getTableFile() {
		return tableFile;
	}
}