package org.jnetwork.ui.database.tools;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JOptionPane;
import javax.swing.JTable;

import org.jnetwork.database.EntrySet;
import org.jnetwork.ui.database.Main;

public class RowSelectionPopupListener extends MouseAdapter {
	private JTable table;
	private EntrySet entries;

	public RowSelectionPopupListener(JTable table, EntrySet entries) {
		this.table = table;
		this.entries = entries;
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
		if (table.getSelectedRow() < entries.size()) {
			RowSelectionPopup menu = new RowSelectionPopup(table, entries, entries.getEntries()[table.getSelectedRow()],
					table.getSelectedRow());
			menu.show(e.getComponent(), e.getX(), e.getY());
		} else {
			JOptionPane.showMessageDialog(Main.MAIN_FRAME, "You must commit this entry before editing it.",
					"Error Editing", JOptionPane.WARNING_MESSAGE);
		}
	}
}
