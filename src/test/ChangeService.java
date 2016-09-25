package test;

import java.io.IOException;
import java.util.ArrayList;

import org.jnetwork.database.QueryException;
import org.jnetwork.database.Table;

public class ChangeService {
	private static ChangeService theService;
	private Table table;
	private ArrayList<Change> changes = new ArrayList<>();
	private ArrayList<Change> permanentChanges = new ArrayList<>();

	private ChangeService(Table table) {
		this.table = table;
	}

	public static ChangeService getService() {
		return theService;
	}

	public static void setService(Table table) {
		ChangeService.theService = new ChangeService(table);
	}

	public ArrayList<Change> getChanges() {
		return changes;
	}

	public void setChanges(ArrayList<Change> changes) {
		this.changes = changes;
	}

	public void change(Change change) {
		if (changes.contains(change)) {
			changes.remove(change);
		}
		if (permanentChanges.contains(change)) {
			permanentChanges.remove(change);
		}

		changes.add(change);
		permanentChanges.add(change);
	}

	public void unchange(Change change) {
		if (changes.contains(change)) {
			changes.remove(change);
		}
		if (permanentChanges.contains(change)) {
			permanentChanges.remove(change);
		}
	}

	public Change getChange(String id) {
		for (Change change : permanentChanges) {
			if (change.getEntryID() != null && change.getEntryID().equals(id)) {
				return change;
			}
		}
		return null;
	}

	public void commitChanges() throws IOException, QueryException {
		int r = 0;
		int c = 0;
		int a = 0;
		DatabaseGUI.getGUI().setIgnoreChanges(true);

		for (Change change : changes) {
			DatabaseGUI.getGUI().clearChangeColors(DatabaseGUI.getGUI().indexOf(change.getData()));

			if (change.getChange() == Change.REMOVE) {
				DatabaseGUI.getGUI().removeRow(DatabaseGUI.getGUI().indexOf(change.getEntryID()));
			}
		}
		for (Change change : changes) {
			if (change.getChange() == Change.SET) {
				table.query("SET ENTRY " + change.getEntryID() + " TO [" + change.getChangeString() + "] IN "
						+ table.getName());
				c++;
			} else if (change.getChange() == Change.REMOVE) {
				table.query("REMOVE ENTRY " + change.getEntryID() + " IN " + table.getName());
				r++;
			} else if (change.getChange() == Change.ADD) {
				table.query("ADD [" + change.getChangeString() + "] IN " + table.getName());
				a++;
			}
		}
		DatabaseGUI.getGUI().setStatus("Changed " + (r + c + a) + " entr" + ((r + c) > 1 ? "ies" : "y") + " (" + c
				+ " changed, " + r + " removed, and " + a + " added).");
		DatabaseGUI.getGUI().setIgnoreChanges(false);
		DatabaseService.getDatabase().setEntries(DatabaseService.getDatabase().getTable()
				.query("GET IN " + DatabaseService.getDatabase().getTable().getName()));
		changes.clear();
	}
}
