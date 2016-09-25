package org.jnetwork.ui.database.tools;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JTable;

import org.jnetwork.database.Entry;
import org.jnetwork.ui.database.Change;
import org.jnetwork.ui.database.ChangeService;
import org.jnetwork.ui.database.DataTableModel;
import org.jnetwork.ui.database.DatabaseGUI;
import org.jnetwork.ui.database.DatabaseService;
import org.jnetwork.ui.database.RowPropertiesWindow;

public class RowSelectionPopup extends ApplicationPopupMenu {
	private static final long serialVersionUID = -5618319824062379597L;

	public RowSelectionPopup(JTable table, Entry entry, int row) {
		DataTableModel model = (DataTableModel) table.getModel();

		JMenuItem item;
		add(item = new JMenuItem(new AbstractAction(model.isDeleted(row) ? "Unmark for Removal" : "Mark for Removal") {
			private static final long serialVersionUID = -6702610578991637167L;

			@Override
			public void actionPerformed(ActionEvent e) {
				DatabaseGUI.getGUI().setIgnoreChanges(true);
				if (model.isDeleted(row)) {
					model.clearRowColor(row);
					ChangeService.getService().unchange(ChangeService.getService().getChange(entry.getEntryID()));
					DatabaseGUI.getGUI().setCanCommit(ChangeService.getService().getChanges().size() > 0);
				} else {
					model.setRowColor(row, Color.RED);
					ChangeService.getService().change(new Change(entry.getEntryID(), Change.REMOVE,
							new ArrayList<>(Arrays.asList(entry.getData())), null));
					DatabaseGUI.getGUI().setCanCommit(true);
				}
				DatabaseGUI.getGUI().setIgnoreChanges(false);
			}
		}));
		item.setIcon(new ImageIcon("assets/RedX.png"));
		add(item = new JMenuItem(new AbstractAction("Properties") {
			private static final long serialVersionUID = 6076051152979327786L;

			@Override
			public void actionPerformed(ActionEvent e) {
				RowPropertiesWindow window = new RowPropertiesWindow(DatabaseService.getDatabase().getTable(), entry);
				window.open();
			}
		}));
	}
}