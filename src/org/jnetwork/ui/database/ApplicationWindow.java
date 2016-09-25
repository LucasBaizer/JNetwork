package org.jnetwork.ui.database;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;

import javax.swing.JDialog;

public abstract class ApplicationWindow extends JDialog {
	private static final long serialVersionUID = 7849708653284764709L;

	public ApplicationWindow(String title) {
		super(Main.MAIN_FRAME);

		this.setTitle(title);
	}

	private boolean allowPacking = true;

	public void open() {
		getContentPane().add(getApplicationPanel());
		setVisible(true);
		if (allowPacking)
			pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height / 2 - this.getSize().height / 2);
	}

	public abstract Component getApplicationPanel();

	public boolean allowsPacking() {
		return allowPacking;
	}

	public void setAllowPacking(boolean allowPacking) {
		this.allowPacking = allowPacking;
	}
}
