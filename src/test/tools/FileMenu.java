package test.tools;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.KeyStroke;
import javax.swing.filechooser.FileFilter;

import org.jnetwork.database.Table;

import test.DatabaseGUI;
import test.Main;

public class FileMenu extends ApplicationMenu {
	private static final long serialVersionUID = 7827201831801283896L;

	@SuppressWarnings("serial")
	public FileMenu() {
		super("File");

		add(new AbstractAction("Open") {
			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser file = new JFileChooser(new File(System.getProperty("user.dir")));
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
