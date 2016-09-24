package org.jnetwork.database;

public class SetQuery extends DependencyQuery {
	private static final long serialVersionUID = 8734671822750697325L;

	public SetQuery() {
		setAction(Query.ACTION_SET);
	}
}
