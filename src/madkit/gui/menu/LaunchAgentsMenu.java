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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.AgentAction;
import madkit.action.MKAbstractAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.MadkitClassLoader;

/**
 * This class builds a {@link JMenu} containing all the 
 * agents which are on the class path, so that they can be
 * individually launched.
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * 
 */
public class LaunchAgentsMenu extends JMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 634377755586801986L;
	final static private Set<LaunchAgentsMenu> menus = new HashSet<LaunchAgentsMenu>(); 
	final private Action myAction;
	final private AbstractAgent myAgent;

	/**
	 * Builds a new menu.
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 */
	public LaunchAgentsMenu(final AbstractAgent agent) {
		super("Agents");
		setMnemonic(KeyEvent.VK_G);
		myAgent = agent;
		myAction = new  MKAbstractAction(AgentAction.LAUNCH_AGENT.getActionInfo()){
			/**
			 * 
			 */
			private static final long serialVersionUID = 6530886642947530268L;

			@Override
			public void actionPerformed(ActionEvent e) {
				agent.launchAgent(e.getActionCommand(),0,true);
			}
		};
			menus.add(this);
		update();
	}

	/**
	 * Called by the kernel when the class path is modified.
	 * This is for instance the case when the 
	 * {@link MadkitClassLoader#addToClasspath(java.net.URL)}
	 * is used.
	 */
	public static void updateAllMenus() {
		for (LaunchAgentsMenu menu : menus) {
			menu.update();
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

	private void update() {
		removeAll();
		final Set<String> classesToLaunch = myAgent.getMadkitClassLoader().getAllAgentClasses();
		if (classesToLaunch.size() < 20) {
			for (String string : classesToLaunch) {
				addTomenu(myAction, this, string, false);
			}
		}
		else{
			String pckName = null;
			JMenu subMenu = null;
			for (String string : classesToLaunch) {
				String pck = string.substring(0,string.lastIndexOf('.'));
				if(pck.equals(pckName)){
					addTomenu(myAction, subMenu, string,true);
				}
				else{
					pckName = pck;
					subMenu = new JMenu(pck);
					add(subMenu);
					addTomenu(myAction, subMenu, string,true);
				}
			}
		}
		
	}

}
