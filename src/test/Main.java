package test;

import java.awt.Font;

import javax.swing.JFrame;
import javax.swing.UIManager;

import test.tools.DatabaseToolbar;

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
	}

	public static final JFrame MAIN_FRAME = new JFrame("Database Editor");

	public static void main(String[] args) {
		DatabaseService.loadFromCache();

		MAIN_FRAME.add(new DatabaseGUI());
		MAIN_FRAME.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		MAIN_FRAME.setJMenuBar(new DatabaseToolbar());
		MAIN_FRAME.setVisible(true);
		MAIN_FRAME.pack();
	}
}
