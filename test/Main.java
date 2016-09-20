package test;

import javax.swing.JFrame;
import javax.swing.UIManager;

import test.toolbar.DatabaseToolbar;

public class Main {
	static {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static final JFrame MAIN_FRAME = new JFrame("Database Editor");

	public static void main(String[] args) {
		MAIN_FRAME.add(new DatabaseGUI());
		MAIN_FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MAIN_FRAME.setJMenuBar(new DatabaseToolbar());
		MAIN_FRAME.setVisible(true);
		MAIN_FRAME.pack();
	}
}
