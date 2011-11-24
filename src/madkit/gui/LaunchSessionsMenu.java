package madkit.gui;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.kernel.AAAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.KernelAction;
import madkit.kernel.KernelAddress;
import madkit.kernel.MKAbstractAction;
import madkit.messages.KernelMessage;

public class LaunchSessionsMenu extends JMenu {

	final static private Set<LaunchSessionsMenu> menus = new HashSet<LaunchSessionsMenu>(); 
	final private AbstractAgent myAgent;

	public LaunchSessionsMenu(final AbstractAgent agent) {
		super("Demos");
		setMnemonic(KeyEvent.VK_D);
		myAgent = agent;
		menus.add(this);
		update();
	}

	public static void updateAllMenus() {//TODO facto
		for (LaunchSessionsMenu menu : menus) {
			menu.update();
		}
	}

	private void addTomenu(Action a, JMenu subMenu, DemoModel demo) {
		JMenuItem name = new JMenuItem(a);
		String displayedName = demo.getName();
		name.setText(displayedName);
		name.setToolTipText(demo.toString());
		name.setAccelerator(null);
		name.setActionCommand(displayedName);
		subMenu.add(name);
	}

	private void update() {
		removeAll();
		for(final DemoModel dm : myAgent.getMadkitClassLoader().getAvailableConfigurations()){
			addTomenu(KernelAction.LAUNCH_SESSION.getActionFor(myAgent, dm),this,dm);
		}
	}

}
