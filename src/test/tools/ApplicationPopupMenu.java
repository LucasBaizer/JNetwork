package test.tools;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;

public class ApplicationPopupMenu extends JPopupMenu {
	private static final long serialVersionUID = 6022149924993054806L;

	public void add(AbstractAction action, KeyStroke stroke, String icon) {
		JMenuItem item = new JMenuItem(action);
		item.setAccelerator(stroke);
		item.setIcon(new ImageIcon("assets/" + icon + ".png"));
		add(item);
	}
}
