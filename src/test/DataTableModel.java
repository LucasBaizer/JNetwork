package test;

import javax.swing.table.DefaultTableModel;

import org.jnetwork.database.Table;

public class DataTableModel extends DefaultTableModel {
	private static final long serialVersionUID = 6446945404601179255L;

	public DataTableModel(Table table) {
		super(table.getColumnHeaders(), 0);
	}

	@Override
	public boolean isCellEditable(int row, int column) {
		return true;
	}
}
