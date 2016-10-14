package org.jnetwork.ui.database;

import java.awt.Color;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.TreeMap;

import javax.swing.table.DefaultTableModel;

import org.jnetwork.database.Table;

public class DataTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 6446945404601179255L;

	private TreeMap<Integer, Color> rowColors = new TreeMap<>();

	public DataTableModel(Table table) {
		super(table.getColumnHeaders(), 0);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}

	public void setRowColor(int row, Color c) {
		rowColors.put(row, c);
		fireTableRowsUpdated(row, row);
	}

	public Color getRowColor(int row) {
		return rowColors.get(row);
	}

	public void clearRowColor(int row) {
		rowColors.remove(row);
		fireTableRowsUpdated(row, row);
	}

	public int indexOf(ArrayList<Serializable> data) {
		ArrayList<Serializable> toRemove = new ArrayList<>();
		for (Serializable e : data) {
			if (e.toString().equals("*")) {
				toRemove.add(e);
			}
		}
		ArrayList<Serializable> optData = new ArrayList<>(data);
		optData.removeAll(toRemove);
		for (int r = 0; r < getRowCount(); r++) {
			ArrayList<Serializable> entryData = new ArrayList<>();
			for (int c = 0; c < getColumnCount(); c++) {
				entryData.add((Serializable) getValueAt(r, c));
			}
			if (entryData.containsAll(optData)) {
				return r;
			}
		}
		return -1;
	}

	public boolean isUncommitedNewChange(int row) {
		return rowColors.get(row) == Color.GREEN;
	}

	public boolean isDeleted(int row) {
		return rowColors.get(row) == Color.RED;
	}

	public int indexOf(String id) {
		int real = 0;
		for (int r = 0; r < getRowCount(); r++) {
			if (!isUncommitedNewChange(r)) {
				if (DatabaseService.getDatabase().getEntrySet().getEntries()[real].getEntryID().equals(id)) {
					return r;
				}
				real++;
			}
		}
		return -1;
	}

	public int indexOfUncommited(ArrayList<Serializable> data) {
		ArrayList<Serializable> toRemove = new ArrayList<>();
		for (Serializable e : data) {
			if (e.toString().equals("*")) {
				toRemove.add(e);
			}
		}
		ArrayList<Serializable> optData = new ArrayList<>(data);
		optData.removeAll(toRemove);
		for (int r = 0; r < getRowCount(); r++) {
			if (rowColors.get(r) != null) {
				ArrayList<Serializable> entryData = new ArrayList<>();
				for (int c = 0; c < getColumnCount(); c++) {
					entryData.add((Serializable) getValueAt(r, c));
				}
				if (entryData.containsAll(optData)) {
					return r;
				}
			}
		}
		return -1;
	}
}
