package org.jnetwork.database;

public class GetQuery extends DependencyQuery {
	private static final long serialVersionUID = 8240638338401062113L;

	public GetQuery() {
		setAction(Query.ACTION_GET);
	}
}
