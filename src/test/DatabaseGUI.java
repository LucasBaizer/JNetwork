package test;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.jnetwork.database.Entry;
import org.jnetwork.database.EntrySet;
import org.jnetwork.database.QueryException;
import org.jnetwork.database.Table;

import test.tools.AutoCompleteDocument;
import test.tools.RowSelectionPopupListener;

public class DatabaseGUI extends JPanel {
	private static final long serialVersionUID = 2996153710300873161L;
	private static DatabaseGUI gui;
	private Table table;
	private JTable jtable;
	private JLabel status = new JLabel("Nothing changed.");

	public DatabaseGUI() {
		gui = this;

		mainPanel.setFocusable(true);
		mainPanel.requestFocusInWindow();
		toolbarPanel.setFocusable(true);
		toolbarPanel.requestFocusInWindow();

		mainPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0),
				"remove");
		mainPanel.getActionMap().put("remove", new AbstractAction() {
			private static final long serialVersionUID = 2734945193611418315L;

			@Override
			public void actionPerformed(ActionEvent e) {
				removeSelectedRows();
			}
		});

		if (DatabaseService.getDatabase() != null) {
			try {
				setTable(DatabaseService.getDatabase().getTable());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void setTable(Table load) throws IOException {
		this.table = load;
		ChangeService.setService(table);
		DatabaseService.setDatabase(table);

		query("GET");
	}

	private String lastQuery = "GET WHERE Column IS Value";
	private JTextField queryField = new JTextField(45);
	private JButton commitButton = new JButton(new ImageIcon("assets/OrangeArrow.png"));
	private IPanel toolbarPanel = new IPanel();
	private IPanel mainPanel = new IPanel();
	private boolean canCommit = false;
	private boolean ignoreChanges = false;

	public void query(String query) throws IOException {
		EntrySet data;
		try {
			data = table.query(query + " IN " + table.getName());
		} catch (QueryException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error Querying", JOptionPane.ERROR_MESSAGE);
			return;
		}

		removeAll();
		mainPanel.removeAll();
		toolbarPanel.removeAll();
		jtable = new JTable(new DataTableModel(table));
		jtable.setModel(new DataTableModel(table));
		jtable.setDefaultRenderer(Object.class, new DataTableRenderer());
		DataTableModel model = (DataTableModel) jtable.getModel();

		EntrySet entries;
		for (Entry entry : (entries = data).getEntries()) {
			model.addRow(entry.getData());
		}

		jtable.addMouseListener(new RowSelectionPopupListener(jtable));
		jtable.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (!ignoreChanges) {
					if (e.getType() == TableModelEvent.UPDATE) {
						ignoreChanges = true;
						int row = e.getLastRow();

						if (row < DatabaseService.getDatabase().getEntrySet().getEntries().length) {
							Entry entry = DatabaseService.getDatabase().getEntrySet().getEntries()[row];
							Change change = new Change(entry.getEntryID(), Change.SET, new ArrayList<>(),
									new ArrayList<>());
							Change previous = ChangeService.getService().getChange(entry.getEntryID());
							if (previous != null) {
								change.setOriginalData(previous.getOriginalData());
							} else {
								change.setOriginalData(new ArrayList<>(Arrays.asList(entry.getData())));
							}

							for (int col = 0; col < jtable.getColumnCount(); col++) {
								change.getData().add((Serializable) jtable.getValueAt(row, col));
							}

							model.setRowColor(row, Color.orange);
							ChangeService.getService().change(change);
							setCanCommit(true);
							ignoreChanges = false;
						} else {
							JOptionPane.showMessageDialog(null, "You must commit this entry before editing it.",
									"Error Editing", JOptionPane.WARNING_MESSAGE);
						}
					}
				}
			}
		});

		DatabaseService.getDatabase().setEntries(entries);

		JButton queryButton = new JButton("Query");
		queryField.setText(lastQuery);
		AutoCompleteDocument auto = new AutoCompleteDocument(queryField);

		queryField.getDocument().addDocumentListener(auto);
		queryField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0), "autocomplete");
		queryField.getActionMap().put("autocomplete", auto.getAutocompleteAction());
		mainPanel.add(queryField, 0, 0);
		mainPanel.c.anchor = GridBagConstraints.NORTHEAST;
		mainPanel.add(queryButton, 0, 0);

		queryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (canCommit) {
					if (JOptionPane.showConfirmDialog(null,
							"Requerying the table will remove your uncommited changes. Continue?",
							"Discard Uncommited Changes?",
							JOptionPane.YES_NO_CANCEL_OPTION) != JOptionPane.YES_OPTION) {
						return;
					}
				}
				ChangeService.getService().setChanges(new ArrayList<>());
				setCanCommit(false);

				try {
					lastQuery = queryField.getText();
					table.query(queryField.getText() + " IN " + table.getName());
					query(lastQuery.toLowerCase().startsWith("get") ? lastQuery : "GET");
				} catch (QueryException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Error Querying", JOptionPane.ERROR_MESSAGE);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}
		});

		if (commitButton.getActionListeners().length > 0) {
			commitButton.removeActionListener(commitButton.getActionListeners()[0]);
		}
		commitButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CommitChangesWindow window = new CommitChangesWindow(table);
				window.open();
			}
		});
		JButton addButton = new JButton(new ImageIcon("assets/GreenPlus.png"));
		addButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				AddEntryWindow window = new AddEntryWindow();
				window.open();
			}
		});

		canCommit = false;
		commitButton.setEnabled(false);
		toolbarPanel.add(addButton, 0, 0);
		toolbarPanel.add(commitButton, 0, 1);

		addButton.setToolTipText("Add Entry");
		commitButton.setToolTipText("Commit Changes");

		mainPanel.c.anchor = GridBagConstraints.NORTHWEST;
		mainPanel.add(status, 0, 1);
		mainPanel.add(new JScrollPane(jtable), 0, 2);

		add(toolbarPanel);
		add(mainPanel);
		Main.MAIN_FRAME.pack();
	}

	private void removeSelectedRows() {
		DefaultTableModel model = (DefaultTableModel) jtable.getModel();
		int[] rows = jtable.getSelectedRows();
		for (int i = 0; i < rows.length; i++) {
			model.removeRow(rows[i] - i);
		}
	}

	public static DatabaseGUI getGUI() {
		return gui;
	}

	public void addTableRow(ArrayList<Serializable> data) {
		ignoreChanges = true;
		DataTableModel model = (DataTableModel) jtable.getModel();
		model.addRow(data.toArray(new Serializable[data.size()]));
		model.setRowColor(model.getRowCount() - 1, Color.GREEN);

		repaint();
		ignoreChanges = false;
	}

	public boolean canCommit() {
		return canCommit;
	}

	public void setCanCommit(boolean canCommit) {
		this.canCommit = canCommit;

		commitButton.setEnabled(canCommit);
		mainPanel.repaint();
	}

	public void setQueryText(String query) {
		queryField.setText(query);
		lastQuery = query;
		mainPanel.repaint();
	}

	public void setStatus(String status) {
		this.status.setText(status);
		mainPanel.repaint();
	}

	public void setIgnoreChanges(boolean ignore) {
		this.ignoreChanges = ignore;
	}

	public void clearChangeColors(int row) {
		DataTableModel model = (DataTableModel) jtable.getModel();
		model.clearRowColor(row);
	}

	public int indexOf(ArrayList<Serializable> string) {
		return ((DataTableModel) jtable.getModel()).indexOf(string);
	}

	public void removeRow(int row) {
		((DataTableModel) jtable.getModel()).removeRow(row);
	}

	public int indexOf(String id) {
		return ((DataTableModel) jtable.getModel()).indexOf(id);
	}
}
