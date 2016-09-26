package org.jnetwork.ui.database.tools.toolbar;

import javax.swing.JMenuBar;

public class DatabaseToolbar extends JMenuBar {
	private static final long serialVersionUID = 4116058101937763309L;

	public DatabaseToolbar() {
		add(new FileMenu());
		add(new TableMenu());
	}
}
