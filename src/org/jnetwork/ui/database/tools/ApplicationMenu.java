package org.jnetwork.ui.database.tools;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.KeyStroke;

public class ApplicationMenu extends JMenu {
	private static final long serialVersionUID = -5579196987442495051L;

	public ApplicationMenu(String string) {
		super(string);
	}

	public void add(AbstractAction action, KeyStroke stroke, String icon) {
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(stroke);
		item.setIcon(new ImageIcon("assets/" + icon + ".png"));
		add(item);
	}
}
