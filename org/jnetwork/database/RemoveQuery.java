package org.jnetwork.database;

public class RemoveQuery extends DependencyQuery {
	private static final long serialVersionUID = -4736790716755991966L;

	public RemoveQuery() {
		setAction(Query.ACTION_REMOVE);
	}
}
