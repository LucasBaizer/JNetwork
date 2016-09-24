package test.tools;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;

import org.jnetwork.database.Entry;

import test.DatabaseStatus;
import test.Main;

public class AutoCompleteDocument implements DocumentListener {
	private JWindow popupWindow = new JWindow(Main.MAIN_FRAME);
	private JTextField textField;

	public AutoCompleteDocument(JTextField field) {
		this.textField = field;
		textField.getInputMap().put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "close");
		textField.getActionMap().put("close", new AbstractAction() {
			private static final long serialVersionUID = 193446136444109585L;

			@Override
			public void actionPerformed(ActionEvent e) {
				popupWindow.setVisible(false);
			}
		});
	}

	public Action getAutocompleteAction() {
		return new AbstractAction("autocompleteAction") {
			private static final long serialVersionUID = -4500981704424301778L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// checkAutocomplete();
			}
		};
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		checkAutocomplete(e);
	}

	private void checkAutocomplete(DocumentEvent e) {
		JPanel contentPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.NORTHWEST;
		popupWindow.setVisible(false);

		try {
			int pos = e.getOffset() + 1;
			String content = textField.getText(0, pos);

			if (content.length() - content.replace(" ", "").length() <= 2
					&& Pattern.compile(".+?((?i)entry )").matcher(content).find()) {
				String idStart = content.substring(content.lastIndexOf(' ') + 1);

				ArrayList<String> possibleIDs = new ArrayList<>();
				for (Entry entry : DatabaseStatus.getDatabase().getEntrySet()) {
					if (idStart.isEmpty() || entry.getEntryID().startsWith(idStart)) {
						possibleIDs.add(entry.getEntryID());
					}
				}
				for (String id : possibleIDs) {
					JLabel label = new JLabel(id);
					label.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseReleased(MouseEvent e) {
							textField.setText(textField.getText() + id + " ");
						}
					});
					contentPanel.add(label, c);
					c.gridy++;
				}
			} else if (content.trim().isEmpty()) {
				String[] actions = { "GET", "ADD", "REMOVE", "SET", "DROP" };
				for (String str : actions) {
					JLabel label = new JLabel(str);
					label.addMouseListener(new MouseAdapter() {
						@Override
						public void mouseReleased(MouseEvent e) {
							textField.setText(textField.getText() + str + " ");
						}
					});
					contentPanel.add(label, c);
					c.gridy++;
				}
			}

			if (contentPanel.getComponentCount() > 0) {
				popupWindow.setSize(new Dimension(150, 50));
				popupWindow.getContentPane().removeAll();
				JScrollPane pane = new JScrollPane(contentPanel);
				popupWindow.getContentPane().add(pane);
				popupWindow.toFront();
				popupWindow.requestFocusInWindow();
				popupWindow.setVisible(true);

				int windowX = 0;
				int windowY = 0;

				windowX = Main.MAIN_FRAME.getX() + textField.getX();
				windowY = Main.MAIN_FRAME.getY() + textField.getY() + textField.getHeight() + popupWindow.getHeight();
				popupWindow.setLocation(windowX, windowY);
			}
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		checkAutocomplete(e);
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
	}
}
