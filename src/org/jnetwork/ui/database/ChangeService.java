package org.jnetwork.ui.database;

import java.io.IOException;
import java.util.ArrayList;

import org.jnetwork.database.QueryException;

public class ChangeService {
	private static ChangeService service = new ChangeService();
	private ArrayList<Change> changes = new ArrayList<>();
	private ArrayList<Change> permanentChanges = new ArrayList<>();

	public static ChangeService getService() {
		return service;
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
			if (change.getChangeID().equals(id)) {
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

		ArrayList<Integer> remove = new ArrayList<>();
		for (Change change : changes) {
			if (change.getChange() != Change.REMOVE) {
				DatabaseGUI.getGUI().clearChangeColors(
						change.getEntryID() == null ? DatabaseGUI.getGUI().indexOfUncommited(change.getData())
								: DatabaseGUI.getGUI().indexOf(change.getEntryID()));
			} else {
				remove.add(DatabaseGUI.getGUI().indexOf(change.getEntryID()));
			}
		}
		for (Change change : changes) {
			if (change.getChange() == Change.SET) {
				DatabaseService.getConnection().query("SET ENTRY " + change.getEntryID() + " TO ["
						+ change.getChangeString() + "] IN " + DatabaseService.getCurrentTableName());
				c++;
			} else if (change.getChange() == Change.REMOVE) {
				DatabaseService.getConnection()
						.query("REMOVE ENTRY " + change.getEntryID() + " IN " + DatabaseService.getCurrentTableName());
				r++;
			} else if (change.getChange() == Change.ADD) {
				DatabaseService.getConnection()
						.query("ADD [" + change.getChangeString() + "] IN " + DatabaseService.getCurrentTableName());
				a++;
			}
		}

		int removed = 0;
		for (int toRemove : remove) {
			DatabaseGUI.getGUI().removeRow(toRemove - removed++);
		}
		DatabaseGUI.getGUI().setStatus("Changed " + (r + c + a) + " entr" + ((r + c + a) != 1 ? "ies" : "y") + " (" + c
				+ " changed, " + r + " removed, and " + a + " added).");
		DatabaseGUI.getGUI().setIgnoreChanges(false);
		changes.clear();
	}
}
