package org.jnetwork.ui.database.tools;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.jnetwork.database.ColumnHeader;
import org.jnetwork.database.Table;
import org.jnetwork.ui.database.DatabaseGUI;
import org.jnetwork.ui.database.DatabaseService;
import org.jnetwork.ui.database.Main;

public class FileMenu extends ApplicationMenu {
	private static final long serialVersionUID = 7827201831801283896L;

	private File f;

	@SuppressWarnings("serial")
	public FileMenu() {
		super("File");

		String username = System.getProperty("user.name");
		String osx = "/Users/" + username + "/Documents";
		String windows = (System.getProperty("user.dir").substring(0, 1)) + ":\\Users\\" + username + "\\Documents";
		String linux = "/usr/" + username + "/Desktop";
		String fileSystem = System.getProperty("os.name").toLowerCase().contains("windows") ? windows
				: System.getProperty("os.name").toLowerCase().contains("mac") ? osx : linux;
		f = DatabaseService.getDatabase() != null ? (DatabaseService.getDatabase().getTable() != null
				? new File(DatabaseService.getDatabase().getTable().getTableFile().getParent()) : new File(fileSystem))
				: new File(fileSystem);
		if (!f.exists()) {
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
					String tableName = JOptionPane.showInputDialog(null, "Enter a name for the new table.",
							"Create New Table", JOptionPane.INFORMATION_MESSAGE);
					if (tableName == null || tableName.trim().isEmpty()) {
						JOptionPane.showMessageDialog(null, "Cannot create a table with an empty name!",
								"Error Creating New Table", JOptionPane.ERROR_MESSAGE);
					}

					File set = new File(file.getSelectedFile().getPath() + "\\" + tableName.trim() + ".table");

					if (set.exists()) {
						if (JOptionPane.showConfirmDialog(null,
								"A table with this name already exists in the directory. Do you wish to overwrite it?",
								"Create New Table", JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) {
							return;
						}
						set.delete();
					}

					try {
						ArrayList<ColumnHeader> columnHeaders = new ArrayList<>();
						while (true) {
							String input = JOptionPane.showInputDialog(null,
									"Enter a header for a column in the new table. Press Cancel to stop creating columns.",
									"Create New Table Column", JOptionPane.INFORMATION_MESSAGE);
							if (input == null || input.isEmpty()) {
								break;
							}
							String type = (String) JOptionPane.showInputDialog(null,
									"Enter a storage type for the " + input + " column.", "Create New Table Column",
									JOptionPane.INFORMATION_MESSAGE, null,
									new String[] { "string", "integer", "decimal" }, "string");
							columnHeaders.add(new ColumnHeader(input,
									type.equals("string") ? ColumnHeader.STORAGE_TYPE_STRING
											: type.equals("integer") ? ColumnHeader.STORAGE_TYPE_INTEGER
													: ColumnHeader.STORAGE_TYPE_DECIMAL));
						}
						DatabaseGUI.getGUI().setTable(new Table(file.getSelectedFile().getPath(), tableName,
								columnHeaders.toArray(new ColumnHeader[columnHeaders.size()])));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
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
						return f.isDirectory() || f.getPath().substring(f.getPath().lastIndexOf('.')).equals(".table");
					}
				});
				if (file.showOpenDialog(Main.MAIN_FRAME) == JFileChooser.APPROVE_OPTION) {
					try {
						DatabaseGUI.getGUI().setTable(Table.load(file.getSelectedFile()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_MASK), "GreenArrow");
		add(new AbstractAction("Pull") {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		}, KeyStroke.getKeyStroke(KeyEvent.VK_P, KeyEvent.CTRL_MASK), "GreenArrow_Down");
	}
}
