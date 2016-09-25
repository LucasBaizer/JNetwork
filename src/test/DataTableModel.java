package test;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.table.DefaultTableModel;

import org.jnetwork.database.Table;

public class DataTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 6446945404601179255L;

	private HashMap<Integer, Color> rowColors = new HashMap<>();

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

	public void clearRowColors(int row) {
		rowColors.clear();
		fireTableRowsUpdated(row, row);
	}
}
