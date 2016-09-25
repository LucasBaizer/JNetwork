package test;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
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

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jnetwork.database.QueryException;
import org.jnetwork.database.Table;

public class CommitChangesWindow extends JDialog {
	private static final long serialVersionUID = 2706595842679383918L;

	private Table table;

	public CommitChangesWindow(Table table) {
		super(Main.MAIN_FRAME);

		this.table = table;
	}

	public void open() {
		setTitle("Commit Changes");
		add(new JScrollPane(new CommitChangesWindowContent()));
		setVisible(true);
		pack();
	}

	private class CommitChangesWindowContent extends JPanel {
		private static final long serialVersionUID = -933193607338792285L;

		private GridBagConstraints c = new GridBagConstraints(0, 0, 1, 1, 0, 0, GridBagConstraints.NORTHWEST,
				GridBagConstraints.NONE, new Insets(5, 5, 0, 5), 0, 0);

		public CommitChangesWindowContent() {
			super(new GridBagLayout());

			for (Change change : ChangeService.getService().getChanges()) {
				JCheckBox box = new JCheckBox();
				box.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						if (box.isSelected()) {
							ChangeService.getService().change(change);
						} else {
							ChangeService.getService().unchange(change);
						}
					}
				});
				box.setSelected(true);
				JLabel label = new JLabel((change.getChange() == Change.REMOVE ? "Removed"
						: change.getChange() == Change.SET ? "Changed" : "Added") + " entry "
						+ (change.getChange() != Change.ADD ? change.getEntryID() : "")
						+ (change.getChange() == Change.SET ? " from:"
								: change.getChange() == Change.REMOVE ? "." : "so:"));
				label.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseReleased(MouseEvent e) {
						Toolkit.getDefaultToolkit().getSystemClipboard()
								.setContents(new StringSelection(change.getEntryID()), null);
					}
				});

				JPanel panel = new JPanel();
				panel.add(box);
				panel.add(label);
				add(panel, c);

				c.insets = new Insets(5, 50, 0, 5);
				String text = "";
				if (change.getChange() == Change.SET) {
					int col = 0;
					text = "<html>From:<br>";
					for (Serializable previous : change.getOriginalData()) {
						text += "&nbsp;&nbsp;&nbsp;&nbsp;" + table.getColumnHeaders()[col++].getColumnName() + ": "
								+ previous.toString() + "<br>";
					}
					text += "To:<br>";
					col = 0;
					for (Serializable current : change.getData()) {
						text += "&nbsp;&nbsp;&nbsp;&nbsp;" + table.getColumnHeaders()[col].getColumnName()
								+ (current.toString().equals("*") ? " [unchanged]" : "") + ": "
								+ (current.toString().equals("*") ? change.getOriginalData().get(col).toString()
										: current.toString())
								+ "<br>";
						col++;
					}

					JLabel t = new JLabel(text + "</html>");
					c.gridy++;
					add(t, c);
					c.gridy++;
				} else if (change.getChange() == Change.ADD) {
					int col = 0;
					text = "<html>Data:<br>";
					for (Serializable previous : change.getData()) {
						text += "&nbsp;&nbsp;&nbsp;&nbsp;" + table.getColumnHeaders()[col++].getColumnName() + ": "
								+ previous.toString() + "<br>";
					}

					JLabel t = new JLabel(text + "</html>");
					c.gridy++;
					add(t, c);
					c.gridy++;
				} else {
					c.gridy++;
				}
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
					try {
						ChangeService.getService().commitChanges();
					} catch (IOException e1) {
						e1.printStackTrace();
					} catch (QueryException e1) {
						e1.printStackTrace();
					}
					DatabaseGUI.getGUI().setCanCommit(false);
					dispose();
				}
			});

			cancel.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});

			add(buttonPanel, c);
		}
	}
}
