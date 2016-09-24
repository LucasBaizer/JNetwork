package org.jnetwork.database;

interface CriteriaCallback {
	public void criteriaMet(Entry entry) throws QueryException;
}
