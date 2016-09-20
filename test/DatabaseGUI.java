package test;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import org.jnetwork.database.Entry;
import org.jnetwork.database.EntrySet;
import org.jnetwork.database.QueryException;
import org.jnetwork.database.Table;

public class DatabaseGUI extends JPanel {
	private static final long serialVersionUID = -8616191359961233020L;

	private static DatabaseGUI gui;
	private Table table;
	private GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
			GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0);

	public DatabaseGUI() {
		super(new GridBagLayout());
		gui = this;
	}

	public void setTable(Table load) throws IOException {
		this.table = load;

		query("GET");
	}

	private String lastQuery = "GET WHERE Column IS Value";

	public void query(String query) throws IOException {
		EntrySet data;
		try {
			data = table.query(query + " IN " + table.getName());
		} catch (QueryException e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error Querying", JOptionPane.ERROR_MESSAGE);
			return;
		}

		removeAll();
		JTable jtable = new JTable(new DataTableModel(table));
		DefaultTableModel model = (DefaultTableModel) jtable.getModel();

		for (Entry entry : data.getEntries()) {
			model.addRow(entry.getData());
		}

		JTextField field = new JTextField(45);
		JButton queryButton = new JButton("Query");
		field.setText(lastQuery);
		add(field, c);
		c.anchor = GridBagConstraints.NORTHEAST;
		add(queryButton, c);

		queryButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					lastQuery = field.getText();
					DatabaseGUI.this.query(field.getText());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				return;
			}
		});

		c.anchor = GridBagConstraints.NORTHWEST;
		c.gridx = 0;
		c.gridy = 1;
		add(new JScrollPane(jtable), c);

		c.gridx = 0;
		c.gridy = 0;

		Main.MAIN_FRAME.pack();
	}

	public static DatabaseGUI getGUI() {
		return gui;
	}
}
