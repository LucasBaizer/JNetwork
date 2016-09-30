package org.jnetwork.ui.database.tools.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

import org.jnetwork.ui.database.DatabaseGUI;
import org.jnetwork.ui.database.DatabaseService;
import org.jnetwork.ui.database.tools.ApplicationMenu;

public class TableMenu extends ApplicationMenu {
	private static final long serialVersionUID = 8871877287788385269L;

	public TableMenu() {
		super("Table");

		JMenuItem viewRaw = new JMenuItem(new AbstractAction("View Raw") {
			private static final long serialVersionUID = 7065869274330789784L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (System.getProperty("os.name").toLowerCase().contains("windows")) {
					try {
						Runtime.getRuntime().exec(
								"notepad.exe " + DatabaseService.getDatabase().getTable().getTableFile().getPath());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		viewRaw.setEnabled(DatabaseService.getDatabase().getTable() != null);
		add(viewRaw);
		JMenuItem refresh = new JMenuItem(new AbstractAction("Refresh") {
			private static final long serialVersionUID = 472820348371362068L;

			@Override
			public void actionPerformed(ActionEvent e) {
				DatabaseService.loadFromCache();
				try {
					DatabaseGUI.getGUI().setTable(DatabaseService.getDatabase().getTable());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		refresh.setEnabled(DatabaseService.getDatabase().getTable() != null);
		add(refresh);
	}
}
