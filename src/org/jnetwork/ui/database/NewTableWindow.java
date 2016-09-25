package org.jnetwork.ui.database;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.jnetwork.database.ColumnHeader;
import org.jnetwork.database.Table;

public class NewTableWindow extends ApplicationWindow {
	private static final long serialVersionUID = 3743421160526080683L;

	private File tableFile;
	private String name;

	public NewTableWindow(File tableFile, String name) {
		super("Create Table");
		this.tableFile = tableFile;
		this.name = name;
	}

	@Override
	public Component getApplicationPanel() {
		return new NewTableContent();
	}

	private class NewTableContent extends IPanel {
		private static final long serialVersionUID = -5557005836775456366L;

		private int i = 0;
		private ArrayList<IPanel> columns = new ArrayList<>();

		public NewTableContent() {
			super();
			c.insets = new Insets(5, 5, 5, 5);

			removeAll();

			IPanel columns = new IPanel();
			columns.add(createPanel(), 0, i);

			IPanel buttons = new IPanel();
			JButton addColumn = new JButton(new ImageIcon("assets/GreenPlus.png"));
			addColumn.addActionListener(e -> {
				columns.add(createPanel(), 0, ++i);

				revalidate();
				repaint();
				pack();
			});
			buttons.add(addColumn, 0, 0);
			JButton done = new JButton("Done");
			done.addActionListener(e -> {
				ColumnHeader[] headers = new ColumnHeader[this.columns.size()];
				for (int i = 0; i < headers.length; i++) {
					IPanel col = this.columns.get(i);

					String columnName = ((JTextField) col.getComponent(0)).getText();
					int storageType = ((JComboBox<String>) col.getComponent(1)).getSelectedIndex();

					headers[i] = new ColumnHeader(columnName, storageType);
				}
				try {
					DatabaseGUI.getGUI().setTable(new Table(tableFile.getPath(), name, headers));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				dispose();
			});
			buttons.add(done, 0, 1);
			JButton cancel = new JButton("Cancel");
			cancel.addActionListener(e -> {
				dispose();
			});
			buttons.add(cancel, 1, 1);

			JButton help = new JButton(new ImageIcon("assets/InfoMark.png"));
			help.addActionListener(e -> {
				JOptionPane.showMessageDialog(null,
						"<html>You can create columns for the " + name + " table using this interface."
								+ "<br>Enter the name of the column in the text field, and choose the type of data it stores in the drop-down menu."
								+ "<br>To create a new column, click the green plus button."
								+ "<br>When you're finished creating columns, press Done."
								+ "<br>To cancel creating the table, press Cancel.</html>",
						"Create Table", JOptionPane.INFORMATION_MESSAGE);
			});

			help.setPreferredSize(new Dimension(30, 30));

			add(help, 0, 0);
			add(columns, 0, 1);
			add(buttons, 0, 2);
		}

		public IPanel createPanel() {
			IPanel panel = new IPanel();
			panel.c.fill = GridBagConstraints.VERTICAL;
			panel.c.insets = new Insets(0, 0, 1, 5);

			JTextField field = new JTextField(25);
			JComboBox<String> drop = new JComboBox<>(new String[] { "integer", "decimal", "string" });

			panel.add(field, 0, 0);
			panel.add(drop, 1, 0);

			columns.add(panel);

			return panel;
		}
	}
}
