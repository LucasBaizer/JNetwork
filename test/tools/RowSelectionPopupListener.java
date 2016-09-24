package test.tools;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JTable;

import test.DatabaseStatus;

public class RowSelectionPopupListener extends MouseAdapter {
	private JTable table;

	public RowSelectionPopupListener(JTable table) {
		this.table = table;
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		if (e.isPopupTrigger()) {
			pop(e);
		}
	}

	private void pop(MouseEvent e) {
		table.getSelectionModel().removeSelectionInterval(table.getSelectedRow(), table.getSelectedRow());
		table.getSelectionModel().addSelectionInterval(table.rowAtPoint(e.getPoint()), table.rowAtPoint(e.getPoint()));
		RowSelectionPopup menu = new RowSelectionPopup(table,
				DatabaseStatus.getDatabase().getEntrySet().getEntries()[table.getSelectedRow()], table.getSelectedRow());
		menu.show(e.getComponent(), e.getX(), e.getY());
	}
}
