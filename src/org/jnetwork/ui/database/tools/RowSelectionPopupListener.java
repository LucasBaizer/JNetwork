package org.jnetwork.ui.database.tools;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.jnetwork.ui.database.DatabaseService;
import org.jnetwork.ui.database.Main;

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
		if (table.getSelectedRow() < DatabaseService.getDatabase().getEntrySet().getEntries().length) {
			RowSelectionPopup menu = new RowSelectionPopup(table,
					DatabaseService.getDatabase().getEntrySet().getEntries()[table.getSelectedRow()],
					table.getSelectedRow());
			menu.show(e.getComponent(), e.getX(), e.getY());
		} else {
			JOptionPane.showMessageDialog(Main.MAIN_FRAME, "You must commit this entry before editing it.",
					"Error Editing", JOptionPane.WARNING_MESSAGE);
		}
	}
}
