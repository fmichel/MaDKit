/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menu;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.KernelAction;
import madkit.gui.DemoModel;
import madkit.kernel.AbstractAgent;
import madkit.kernel.MadkitClassLoader;

/**
 * This class builds a {@link JMenu} containing all the 
 * MadKit sessions which have been found on the class path, so that they can be
 * individually launched.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public class LaunchSessionMenu extends JMenu {

	private static final long serialVersionUID = 6721458300016754609L;
	final static private Set<LaunchSessionMenu> menus = new HashSet<LaunchSessionMenu>(); 
	final private AbstractAgent myAgent;

	/**
	 * Builds a new menu.
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 */
	public LaunchSessionMenu(final AbstractAgent agent) {
		super("Demos");
		setMnemonic(KeyEvent.VK_D);
		myAgent = agent;
		menus.add(this);
		update();
	}

	/**
	 * Called by the kernel when the class path is modified.
	 * This is for instance the case when the 
	 * {@link MadkitClassLoader#addToClasspath(java.net.URL)}
	 * is used.
	 */
	public static void updateAllMenus() {//TODO facto
		for (LaunchSessionMenu menu : menus) {
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
		if(getItemCount() == 0)
			setVisible(false);
		else
			setVisible(true);
	}

}
