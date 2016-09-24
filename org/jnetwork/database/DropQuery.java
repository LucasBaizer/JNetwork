package org.jnetwork.database;

public class DropQuery extends Query {
	private static final long serialVersionUID = -8225775156293711105L;

	public DropQuery() {
		setAction(Query.ACTION_DROP);
	}
}
