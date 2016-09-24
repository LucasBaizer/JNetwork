package test;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractAction;
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
	private static final long serialVersionUID = -8616191359961233020L;

	private static DatabaseGUI gui;
	private Table table;
	private JTable jtable;
	private JLabel status = new JLabel("Nothing changed.");
	private GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);

	public DatabaseGUI() {
		super(new GridBagLayout());
		gui = this;

		setFocusable(true);
		requestFocusInWindow();

		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_BACK_SPACE, 0), "remove");
		getActionMap().put("remove", new AbstractAction() {
			private static final long serialVersionUID = 2734945193611418315L;

			@Override
			public void actionPerformed(ActionEvent e) {
				removeSelectedRows();
			}
		});

		if (DatabaseStatus.getDatabase() != null) {
			try {
				setTable(DatabaseStatus.getDatabase().getTable());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
	}

	public void setTable(Table load) throws IOException {
		this.table = load;
		ChangeService.setService(table);
		DatabaseStatus.setDatabase(table);

		query("GET");
	}

	private String lastQuery = "GET WHERE Column IS Value";
	private JTextField queryField = new JTextField(45);
	private JButton commitButton = new JButton("Commit");
	private boolean canCommit = false;

	public void query(String query) throws IOException {
		EntrySet data;
		try {
			data = table.query(query + " IN " + table.getName());
		} catch (QueryException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error Querying", JOptionPane.ERROR_MESSAGE);
			return;
		}

		removeAll();
		jtable = new JTable(new DataTableModel(table));
		DefaultTableModel model = (DefaultTableModel) jtable.getModel();

		EntrySet entries;
		for (Entry entry : (entries = data).getEntries()) {
			model.addRow(entry.getData());
		}

		jtable.addMouseListener(new RowSelectionPopupListener(jtable));
		jtable.getModel().addTableModelListener(new TableModelListener() {
			@Override
			public void tableChanged(TableModelEvent e) {
				if (e.getType() == TableModelEvent.UPDATE) {
					int row = e.getLastRow();
					int col = e.getColumn();

					Entry entry = DatabaseStatus.getDatabase().getEntrySet().getEntries()[row];
					if (!entry.getData()[col].toString().equals(jtable.getModel().getValueAt(row, col).toString())) {
						Change change = new Change(entry.getEntryID(), Change.SET, new ArrayList<>(),
								new ArrayList<>());
						Change previous = ChangeService.getService().getChange(entry.getEntryID());
						if (previous != null) {
							change.setOriginalData(previous.getOriginalData());
						} else {
							change.setOriginalData(new ArrayList<>(Arrays.asList(entry.getData())));
						}
						for (int i = 0; i < jtable.getColumnCount(); i++) {
							if (i == col) {
								change.getData().add((Serializable) jtable.getModel().getValueAt(row, col));
							} else {
								if (previous != null) {
									change.getData().add(previous.getData().get(i));
								} else {
									change.getData().add("*");
								}
							}
						}
						ChangeService.getService().change(change);
						setCanCommit(true);
					}
				}
			}
		});

		DatabaseStatus.getDatabase().setEntries(entries);

		JButton queryButton = new JButton("Query");
		queryField.setText(lastQuery);
		AutoCompleteDocument auto = new AutoCompleteDocument(queryField);

		queryField.getDocument().addDocumentListener(auto);
		queryField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_CONTROL, 0), "autocomplete");
		queryField.getActionMap().put("autocomplete", auto.getAutocompleteAction());
		add(queryField, c);
		c.anchor = GridBagConstraints.NORTHEAST;
		add(queryButton, c);

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

		c.gridy = 1;
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

		canCommit = false;
		commitButton.setEnabled(false);
		add(commitButton, c);

		c.anchor = GridBagConstraints.NORTHWEST;
		add(status, c);

		c.gridx = 0;
		c.gridy = 2;
		add(new JScrollPane(jtable), c);

		c.gridx = 0;
		c.gridy = 0;

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

	public boolean canCommit() {
		return canCommit;
	}

	public void setCanCommit(boolean canCommit) {
		this.canCommit = canCommit;

		commitButton.setEnabled(canCommit);
		repaint();
	}

	public void setQueryText(String query) {
		queryField.setText(query);
		lastQuery = query;
		repaint();
	}

	public void setStatus(String status) {
		this.status.setText(status);
		repaint();
	}
}
