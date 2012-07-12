/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.AgentAction;
import madkit.action.MDKAbstractAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.MadkitClassLoader;

/**
 * This class builds a {@link JMenu} containing all the 
 * agents which are on the class path, so that they can be
 * individually launched.
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
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
	final private boolean withKernelAgents;

	/**
	 * Builds a new menu containing all the agents available on the class path.
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 * @param withKernelAgents also take the 4 agents from madkit.kernel if <code>true</code>
	 */
	/**
	 * @param agent
	 */
	public LaunchAgentsMenu(final AbstractAgent agent, boolean withKernelAgents) {
		super("Agents");
		this.withKernelAgents = withKernelAgents;
		setMnemonic(KeyEvent.VK_G);
		myAgent = agent;
		myAction = new  MDKAbstractAction(AgentAction.LAUNCH_AGENT.getActionInfo()){
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
	 * Builds a new menu containing all the agents available on the class path,
	 * except madkit.kernel agents.
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 */
	public LaunchAgentsMenu(final AbstractAgent agent) {
		this(agent,false);
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

	private void addTomenu(final Action a, final JMenu subMenu, final String className, final boolean simpleName) {
		final JMenuItem name = new JMenuItem(a);
		name.setText(simpleName ? MadkitClassLoader.getClassSimpleName(className) : className);
		name.setAccelerator(null);
		name.setActionCommand(className);
		subMenu.add(name);
	}

	private void update() {
		removeAll();
		final Set<String> classesToLaunch = myAgent.getMadkitClassLoader().getAllAgentClasses();
		if(! withKernelAgents)
			for (Iterator<String> iterator = classesToLaunch.iterator(); iterator.hasNext();) {
				if(iterator.next().contains("madkit.kernel"))
						iterator.remove();
			}
		if (classesToLaunch.size() < 20) {
			for (String string : classesToLaunch) {
				addTomenu(myAction, this, string, false);
			}
		}
		else{
			String pckName = null;
			JMenu subMenu = null;
			for (final String string : classesToLaunch) {
				String pck = MadkitClassLoader.getClassPackageName(string);
				if (pck != null) {
					if (pck.equals(pckName)) {
						addTomenu(myAction, subMenu, string, true);
					}
					else {
						pckName = pck;
						subMenu = new JMenu(pck);
						add(subMenu);
						addTomenu(myAction, subMenu, string, true);
					}
				}
			}
		}
		
	}

}
