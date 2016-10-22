package org.jnetwork.ui.database;

import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;

import org.jnetwork.database.QueryException;
import org.jnetwork.ui.database.tools.toolbar.DatabaseToolbar;

public class Main {
	private static Font font = new Font("Courier", Font.PLAIN, 12);

	private static void font(String... keys) {
		for (String key : keys) {
			UIManager.put(key + ".font", font);
		}
	}

	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

			font("Label", "TextField", "Table");
		} catch (Exception e) {
			e.printStackTrace();
		}

		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				if (e instanceof QueryException) {
					JOptionPane.showMessageDialog(Main.MAIN_FRAME, e.getMessage(), "Error Querying",
							JOptionPane.ERROR_MESSAGE);
				} else {
					e.printStackTrace();
				}
			}
		});
	}

	public static final JFrame MAIN_FRAME = new JFrame("Database Editor");

	public static void main(String[] args) {
		MAIN_FRAME.add(new DatabaseGUI());
		MAIN_FRAME.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		MAIN_FRAME.setJMenuBar(new DatabaseToolbar());

		MAIN_FRAME.setVisible(true);
		MAIN_FRAME.pack();

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		MAIN_FRAME.setLocation(dim.width / 2 - MAIN_FRAME.getSize().width / 2,
				dim.height / 2 - MAIN_FRAME.getSize().height / 2);

		MAIN_FRAME.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (ChangeService.getService() != null && ChangeService.getService().getChanges().size() > 0) {
					if (JOptionPane.showConfirmDialog(MAIN_FRAME,
							"Your uncommited changes will be discarded. Continue exiting?", "Exit",
							JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
						MAIN_FRAME.dispose();
					}
				} else {
					if (JOptionPane.showConfirmDialog(MAIN_FRAME, "Are you sure that you want to exit?", "Exit",
							JOptionPane.YES_NO_CANCEL_OPTION) == JOptionPane.YES_OPTION) {
						MAIN_FRAME.dispose();
					}
				}
			}
		});
	}
}
