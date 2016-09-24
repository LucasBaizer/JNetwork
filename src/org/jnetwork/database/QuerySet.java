package org.jnetwork.database;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public class QuerySet implements Serializable, Iterable<Query> {
	private static final long serialVersionUID = 2230234866742778892L;

	private ArrayList<Query> queries = new ArrayList<>();

	public QuerySet() {
	}

	public QuerySet(Query... queries) {
		this.queries = new ArrayList<>(Arrays.asList(queries));
	}

	public void setEntries(Query[] entries) {
		this.queries = new ArrayList<>(Arrays.asList(entries));
	}

	public Query[] getQueries() {
		return this.queries.toArray(new Query[this.queries.size()]);
	}

	public void addQuery(Query e) {
		this.queries.add(e);
	}

	public void removeQuery(Query e) {
		this.queries.remove(e);
	}

	@Override
	public Iterator<Query> iterator() {
		return queries.iterator();
	}
}
