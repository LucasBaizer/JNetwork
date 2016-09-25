package org.jnetwork.ui.database;

import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jnetwork.database.QueryException;
import org.jnetwork.database.Table;

public class CommitChangesWindow extends ApplicationWindow {
	private static final long serialVersionUID = 2706595842679383918L;

	private Table table;

	public CommitChangesWindow(Table table) {
		super("Commit Changes");

		this.table = table;
	}

	private class CommitChangesWindowContent extends JScrollPane {
		private static final long serialVersionUID = -933193607338792285L;

		private GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0);

		private ArrayList<Change> changesToNotCommit = new ArrayList<>();

		public CommitChangesWindowContent() {
			JPanel panel = new JPanel();

			for (Change change : ChangeService.getService().getChanges()) {
				JCheckBox box = new JCheckBox();
				box.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (box.isSelected()) {
							changesToNotCommit.remove(change);
						} else {
							changesToNotCommit.add(change);
						}
					}
				});
				box.setSelected(true);
				JLabel label = new JLabel("<html>"
						+ (change.getChange() == Change.REMOVE ? "Removed"
								: change.getChange() == Change.SET ? "Changed" : "Added")
						+ " entry " + (change.getChange() != Change.ADD ? change.getEntryID() : "")
						+ (change.getChange() == Change.SET ? ":"
								: change.getChange() == Change.REMOVE ? "." : "so:<br>"));
				label.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						Toolkit.getDefaultToolkit().getSystemClipboard()
								.setContents(new StringSelection(change.getEntryID()), null);
					}
				});

				JPanel p2 = new JPanel();
				p2.add(box);
				panel.add(p2, c);

				c.insets = new Insets(5, 50, 0, 5);
				String text = "";
				if (change.getChange() == Change.SET) {
					int col = 0;
					text = "&nbsp;&nbsp;From:<br>";
					for (Serializable previous : change.getOriginalData()) {
						text += "&nbsp;&nbsp;&nbsp;&nbsp;" + table.getColumnHeaders()[col++].getColumnName() + ": "
								+ previous.toString() + "<br>";
					}
					text += "&nbsp;&nbsp;To:<br>";
					col = 0;
					for (Serializable current : change.getData()) {
						text += "&nbsp;&nbsp;&nbsp;&nbsp;" + table.getColumnHeaders()[col].getColumnName()
								+ (current.toString().equals("*") ? " [unchanged]" : "") + ": "
								+ (current.toString().equals("*") ? change.getOriginalData().get(col).toString()
										: current.toString())
								+ "<br>";
						col++;
					}

					label.setText(label.getText() + text + "</html>");
				} else if (change.getChange() == Change.ADD) {
					int col = 0;
					text = "&nbsp;&nbsp;Data:<br>";
					for (Serializable previous : change.getData()) {
						text += "&nbsp;&nbsp;&nbsp;&nbsp;" + table.getColumnHeaders()[col++].getColumnName() + ": "
								+ previous.toString() + "<br>";
					}

					label.setText(label.getText() + text + "</html>");
				}
				c.gridy++;
				panel.add(label);
				c.insets = new Insets(5, 5, 0, 5);
			}

			JPanel buttonPanel = new JPanel(new GridLayout());

			JButton commit = new JButton("Commit");
			JButton cancel = new JButton("Cancel");

			buttonPanel.add(commit);
			buttonPanel.add(cancel);

			commit.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					for (Change c : changesToNotCommit) {
						ChangeService.getService().unchange(c);
					}

					try {
						ChangeService.getService().commitChanges();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (QueryException e1) {
						e1.printStackTrace();
					}

					for (Change c : changesToNotCommit) {
						ChangeService.getService().change(c);
					}

					if (changesToNotCommit.size() == 0) {
						DatabaseGUI.getGUI().setCanCommit(false);
					}
					dispose();
				}
			});

			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			panel.add(buttonPanel, c);

			getViewport().setView(panel);
		}
	}

	@Override
	public Component getApplicationPanel() {
		return new CommitChangesWindowContent();
	}
}
