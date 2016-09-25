package test;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;

public class DataTableRenderer extends DefaultTableCellRenderer {
	private static final long serialVersionUID = -3898732040017364594L;

	@Override
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus,
			int row, int column) {
		DataTableModel model = (DataTableModel) table.getModel();
		Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		c.setBackground(model.getRowColor(row));
		c.setForeground(Color.BLACK);
		return c;
	}
}
