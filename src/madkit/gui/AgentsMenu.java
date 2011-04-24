package madkit.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;


class AgentsMenu extends JMenu {

	private GUIManagerAgent guiManager;

	AgentsMenu(final GUIManagerAgent guiManagerAgent) {
		super("Agents");
		guiManager = guiManagerAgent;
		setMnemonic(KeyEvent.VK_G);
		update();
	}
	
	void update(){
		Action a = MadkitActions.AGENT_LAUNCH_AGENT.getAction(guiManager);
		final Set<String> classesToLaunch = guiManager.getLoadedClasses();
		removeAll();
		if (classesToLaunch.size() < 20) {
			for (String string : classesToLaunch) {
				addTomenu(a, this, string, false);
			}
		}
		else{
			String pckName = null;
			JMenu subMenu = null;
			for (String string : classesToLaunch) {
				String pck = string.substring(0,string.lastIndexOf('.'));
				if(pck.equals(pckName)){
					addTomenu(a, subMenu, string,true);
				}
				else{
					pckName = pck;
					subMenu = new JMenu(pck);
					add(subMenu);
					addTomenu(a, subMenu, string,true);
				}
			}
		}
	}

	private void addTomenu(Action a, JMenu subMenu, String className, boolean simpleName) {
		JMenuItem name = new JMenuItem(a);
		String displayedName = simpleName ? className.substring(className.lastIndexOf('.')+1, className.length()) : className;
		name.setText(displayedName);
		name.setAccelerator(null);
		name.setActionCommand(className);
		subMenu.add(name);
	}

}
