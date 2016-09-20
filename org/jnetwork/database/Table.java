package org.jnetwork.database;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.math.BigInteger;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jnetwork.database.Query.HeaderDependency;

public class Table implements Serializable {
	private static final long serialVersionUID = 7406644564523364391L;

	private String name;
	private ColumnHeader[] columnHeaders;
	private File tableFile;
	private PrintStream out;

	public static Table load(File file) throws IOException {
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

		return new Table(tableName, headers.toArray(new ColumnHeader[headers.size()]));
	}

	public Table(String name, ColumnHeader[] columnHeaders) throws IOException {
		this.setName(name);
		this.columnHeaders = columnHeaders;
		this.tableFile = new File(System.getProperty("user.dir") + "/tables/" + name + ".table");

		if (!tableFile.exists()) {
			new File(System.getProperty("user.dir") + "/tables").mkdir();
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

	public EntrySet query(String request) throws IOException {
		return query(Query.parseQuery(request));
	}

	public EntrySet query(Query request) throws IOException {
		if (request.getAction() == Query.ACTION_ADD) {
			if (request.getData().length != columnHeaders.length) {
				throw new QueryException("Request data array length inequal to column headers array length");
			}

			int i = 0;
			String objectToWrite = "{" + new BigInteger(64, new SecureRandom()).toString(16) + ";";
			for (String obj : request.getData()) {
				if (isInteger(obj)) {
					if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_INTEGER) {
						throw new QueryException("Object '" + obj + "' does not follow column storage type: integer");
					}

					objectToWrite += "I:";
				} else if (isDecimal(obj)) {
					if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_DECIMAL) {
						throw new QueryException("Object '" + obj + "' does not follow column storage type: decimal");
					}

					objectToWrite += "D:";
				} else {
					if (columnHeaders[i].getStorageType() != ColumnHeader.STORAGE_TYPE_STRING) {
						throw new QueryException("Object '" + obj + "' does not follow column storage type: string");
					}

					objectToWrite += "S:";
				}
				objectToWrite += obj + ",";
				i++;
			}
			writeEntry(objectToWrite.substring(0, objectToWrite.length() - 1) + "}");
		} else if (request.getAction() == Query.ACTION_GET) {
			EntrySet data = new EntrySet();
			Matcher matcher = Pattern.compile("\\{(.+?)\\}").matcher(readTable());

			for (HeaderDependency dependency : request.getHeaderDependencies()) {
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
										: dependency.toString()));
				System.out.println(dependency.getValue().getClass() + ": " + dependency.getValue());
			}
			while (matcher.find()) {
				String[] split = matcher.group(1).split(";");
				String id = split[0];
				String content = split[1];

				Entry entry = new Entry(id);
				int i = 0;
				boolean successful = true;
				currentElement: for (String element : content.split(",")) {
					String[] elementSplit = element.split("\\:");
					String type = elementSplit[0];
					String value = elementSplit[1];

					ColumnHeader col = columnHeaders[i];
					Serializable sValue = null;
					if (type.equals("I")) {
						entry.setData(col.getColumnName(), sValue = Integer.parseInt(value));
					} else if (type.equals("D")) {
						entry.setData(col.getColumnName(), sValue = Double.parseDouble(value));
					} else if (type.equals("S")) {
						entry.setData(col.getColumnName(), sValue = value);
					}

					for (HeaderDependency dependency : request.getHeaderDependencies()) {
						if (dependency.getAction() == HeaderDependency.ACTION_EQUALS
								&& dependency.getHeader().equals(col.getColumnName())
								&& (isPrimitive(dependency.getValue().getClass()) ? dependency.getValue() == sValue
										: !dependency.getValue().equals(sValue))) {
							successful = false;
							break currentElement;
						} else if (dependency.getAction() == HeaderDependency.ACTION_NOT_EQUALS
								&& dependency.getHeader().equals(col.getColumnName())
								&& (isPrimitive(dependency.getValue().getClass()) ? dependency.getValue() == sValue
										: dependency.getValue().equals(sValue))) {
							successful = false;
							break currentElement;
						} else if (dependency.getAction() == HeaderDependency.ACTION_CONTAINS
								&& dependency.getHeader().equals(col.getColumnName())
								&& !sValue.toString().contains(dependency.getValue().toString())) {
							successful = false;
							break currentElement;
						} else if (dependency.getAction() == HeaderDependency.ACTION_NOT_CONTAINS
								&& dependency.getHeader().equals(col.getColumnName())
								&& sValue.toString().contains(dependency.getValue().toString())) {
							successful = false;
							break currentElement;
						}
					}
					i++;
				}
				if (successful) {
					data.addEntry(entry);
					entry.setQueryTime(System.currentTimeMillis() - request.getQueryTime());
				}
			}

			return data;
		}
		return null;
	}

	private static final Set<Class<?>> WRAPPER_TYPES = new HashSet<Class<?>>(Arrays.asList(Boolean.class,
			Character.class, Byte.class, Short.class, Integer.class, Long.class, Float.class, Double.class));

	public static boolean isPrimitive(Class<?> clazz) {
		return WRAPPER_TYPES.contains(clazz);
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
}