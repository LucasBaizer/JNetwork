package org.jnetwork.ui.database.tools.toolbar;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.jnetwork.database.Table;
import org.jnetwork.ui.database.DatabaseGUI;
import org.jnetwork.ui.database.DatabaseService;
import org.jnetwork.ui.database.Main;
import org.jnetwork.ui.database.NewTableWindow;
import org.jnetwork.ui.database.tools.ApplicationMenu;

public class FileMenu extends ApplicationMenu {
	private static final long serialVersionUID = 7827201831801283896L;

	private File f;

	@SuppressWarnings("serial")
	public FileMenu() {
		super("File");

		if (f == null || !f.exists()) {
			f = new File(System.getProperty("user.dir"));
		}
		add(new AbstractAction("New") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser file = new JFileChooser(f);
				file.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "Directories";
					}

					@Override
					public boolean accept(File f) {
						return f.isDirectory();
					}
				});
				file.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				if (file.showOpenDialog(Main.MAIN_FRAME) == JFileChooser.APPROVE_OPTION) {
					String tableName = JOptionPane.showInputDialog(Main.MAIN_FRAME, "Enter a name for the new table.",
							"Create New Table", JOptionPane.INFORMATION_MESSAGE);

					if (tableName == null || tableName.trim().isEmpty()) {
						return;
					}

					File set = new File(file.getSelectedFile().getPath() + "\\" + tableName.trim() + ".table");

					if (set.exists()) {
						if (JOptionPane.showConfirmDialog(Main.MAIN_FRAME,
								"A table with this name already exists in the directory. Do you wish to overwrite it?",
								"Create New Table", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							return;
						}
						set.delete();
					}

					NewTableWindow window = new NewTableWindow(file.getSelectedFile(), tableName);
					window.open();
				}
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_N, KeyEvent.CTRL_MASK), "GreenPlus");
		add(new AbstractAction("Open") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser file = new JFileChooser(f);
				file.setFileFilter(new FileFilter() {
					@Override
					public String getDescription() {
						return "JNetwork Database Table Files (*.table)";
					}

					@Override
					public boolean accept(File f) {
						return f.isDirectory() || f.getPath().contains("\\.")
								&& f.getPath().substring(f.getPath().lastIndexOf('.')).equals(".table");
					}
				});
				if (file.showOpenDialog(Main.MAIN_FRAME) == JFileChooser.APPROVE_OPTION) {
					try {
						DatabaseService.startLocalServer();
						DatabaseService.getLocalServerHandler().getDatabase()
								.addTable(Table.load(file.getSelectedFile()));
						DatabaseService.setCurrentTableName(file.getSelectedFile().getName().substring(0,
								file.getSelectedFile().getName().lastIndexOf('.')));

						DatabaseService.connect("localhost", 1337);

						DatabaseGUI.getGUI().query("GET IN " + DatabaseService.getCurrentTableName());
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK), "GreenArrow");
		add(new AbstractAction("Pull") {
			@Override
			public void actionPerformed(ActionEvent e) {
				String ip = JOptionPane.showInputDialog("Please enter the address of the database server.");

				if (ip == null || ip.isEmpty()) {
					return;
				}

				try {
					DatabaseService.connect(ip, 1337);
					String table = JOptionPane.showInputDialog("Please enter the name of the table.");

					if (table == null || table.isEmpty()) {
						return;
					}
					DatabaseService.setCurrentTableName(table);

					DatabaseGUI.getGUI().query("GET IN " + DatabaseService.getCurrentTableName());
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(null, e1.getMessage(), "Error Retrieving Table",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK), "GreenArrow_Down");
	}
}
