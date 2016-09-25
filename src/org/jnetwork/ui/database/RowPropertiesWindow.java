package org.jnetwork.ui.database;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jnetwork.database.ColumnHeader;
import org.jnetwork.database.Entry;
import org.jnetwork.database.Table;

public class RowPropertiesWindow extends JDialog {
	private static final long serialVersionUID = -5096508335843221830L;

	private Table table;
	private Entry entry;

	public RowPropertiesWindow(Table table, Entry entry) {
		super(Main.MAIN_FRAME);

		this.table = table;
		this.entry = entry;
	}

	public void open() {
		setTitle("Entry Properties");
		add(new RowPropertiesWindowContent());
		setVisible(true);
		pack();
	}

	private class RowPropertiesWindowContent extends JPanel {
		private static final long serialVersionUID = -933193607338792285L;

		private GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);

		public RowPropertiesWindowContent() {
			super(new GridBagLayout());

			add("Entry ID: " + entry.getEntryID(), entry.getEntryID(), 0, 0);
			c.insets = new Insets(0, 5, 1, 0);
			int space = 0;
			for (ColumnHeader column : table.getColumnHeaders()) {
				if (column.getColumnName().length() > space) {
					space = column.getColumnName().length();
				}
			}
			for (int i = 0; i < entry.getData().length; i++) {
				add(table.getColumnHeaders()[i].getColumnName() + ": "
						+ (new String(new char[space - table.getColumnHeaders()[i].getColumnName().length()])
								.replaceAll("\0", " "))
						+ entry.getData()[i], entry.getData()[i].toString(), 0, i + 1);
			}
		}

		private void add(String text, String clip, int x, int y) {
			c.gridx = x;
			c.gridy = y;
			JLabel component;
			add(component = new JLabel(text), c);
			component.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseReleased(MouseEvent e) {
					Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(clip), null);
				}
			});
			c.gridx = 0;
			c.gridy = 0;
		}
	}
}
