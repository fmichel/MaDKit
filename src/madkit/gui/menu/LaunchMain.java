/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menu;

import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.GlobalAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.MadkitClassLoader;

/**
 * This class builds a {@link JMenu} containing all the 
 * agent classes containing a main method
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.1
 * @version 0.9
 * 
 */
public class LaunchMain extends JMenu {

	private static final long serialVersionUID = 6721458300016754609L;
	final static private Set<LaunchMain> menus = new HashSet<>();//TODO Map 
	final private AbstractAgent myAgent;

	/**
	 * Builds a new menu.
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 * @param title the title to use
	 */
	public LaunchMain(final AbstractAgent agent, final String title) {
		super(title);
		setMnemonic(KeyEvent.VK_A);
		myAgent = agent;
		synchronized (menus) {
			menus.add(this);
		}
		update();
	}

	/**
	 * Called by the kernel when the class path is modified.
	 * This is for instance the case when the 
	 * {@link MadkitClassLoader#loadUrl(java.net.URL)}
	 * is used.
	 */
	public static void updateAllMenus() {//TODO facto
		synchronized (menus) {
			for (LaunchMain menu : menus) {
				menu.update();
			}
		}
	}

	private void update() {
		if(! myAgent.isAlive())
			return;
		removeAll();
		for (final String string : MadkitClassLoader.getAgentsWithMain()) {
			JMenuItem name = new JMenuItem(GlobalAction.LAUNCH_MAIN);
			name.setActionCommand(string);
			name.setText(string+".main");
			add(name);
		}
		setVisible(getItemCount() != 0);
	}

}
