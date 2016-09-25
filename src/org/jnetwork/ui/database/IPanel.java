package org.jnetwork.ui.database;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JPanel;

public class IPanel extends JPanel {
	private static final long serialVersionUID = 2729391337709207197L;

	public GridBagConstraints c = new GridBagConstraints();

	public IPanel() {
		super(new GridBagLayout());

		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
	}

	public void add(Component c, int x, int y) {
		this.c.gridx = x;
		this.c.gridy = y;
		add(c, this.c);
		this.c.gridx = 0;
		this.c.gridy = 0;
	}
}
