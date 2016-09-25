package test.tools;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.jnetwork.database.Entry;

import test.Change;
import test.ChangeService;
import test.DatabaseGUI;
import test.DatabaseService;
import test.RowPropertiesWindow;

public class RowSelectionPopup extends ApplicationPopupMenu {
	private static final long serialVersionUID = -5618319824062379597L;

	public RowSelectionPopup(JTable table, Entry entry, int row) {
		JMenuItem item;
		add(item = new JMenuItem(new AbstractAction("Delete Row") {
			private static final long serialVersionUID = -6702610578991637167L;

			@Override
			public void actionPerformed(ActionEvent e) {
				((DefaultTableModel) table.getModel()).removeRow(row);

				ChangeService.getService().change(new Change(entry.getEntryID(), Change.REMOVE,
						new ArrayList<>(Arrays.asList(entry.getData())), null));
				DatabaseGUI.getGUI().setCanCommit(true);
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