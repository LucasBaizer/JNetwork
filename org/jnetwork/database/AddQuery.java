package org.jnetwork.database;

public class AddQuery extends Query {
	private static final long serialVersionUID = -4848350020529632315L;

	public AddQuery() {
		setAction(Query.ACTION_ADD);
	}
}
