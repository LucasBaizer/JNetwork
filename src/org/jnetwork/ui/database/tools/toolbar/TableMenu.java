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

		JMenuItem refresh = new JMenuItem(new AbstractAction("Refresh") {
			private static final long serialVersionUID = 472820348371362068L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					DatabaseGUI.getGUI().query("GET IN " + DatabaseService.getCurrentTableName());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		refresh.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_MASK));
		refresh.setEnabled(DatabaseService.isConnected());
		add(refresh);
	}
}
