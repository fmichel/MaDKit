/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
public class LaunchAgentsMenu extends ClassPathSensitiveMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 634377755586801986L;
//	final static private Set<LaunchAgentsMenu> menus = new HashSet<>(); 
	final private Action myAction;
	final private AbstractAgent myAgent;
	private final AgentClassFilter filter;
//	protected final int numberToLaunch;

	/**
	 * Builds a new menu, "Agents", containing all the agents available on the class path.
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 */
	/**
	 * @param agent
	 */
	public LaunchAgentsMenu(final AbstractAgent agent) {
		this(agent,"Agents",null);
	}

	/**
	 * Builds a new menu containing all the agents available on the class path,
	 * except madkit.kernel agents.
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 * @param menuName the name of the menu
	 */
	public LaunchAgentsMenu(final AbstractAgent agent, final String menuName) {
		this(agent,menuName, null);
	}

	/**
	 * Builds a new launching menu containing all 
	 * the agents available on the class path.
	 * 
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 * @param menuName the name of the menu
	 * @param filter help filtering which classes should be included
	 */
	public LaunchAgentsMenu(final AbstractAgent agent, String menuName, AgentClassFilter filter) {
		super(menuName);
		this.filter = filter;
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
		update();
	}

//	private void addLaunchingNumber(ButtonGroup group, final int i) {//TODO
//		final JRadioButton b = new JRadioButton(Words.LAUNCH.toString()+ " : "+i);
//		add(b);
//		b.addActionListener(new ActionListener() {
//			@Override
//			public void actionPerformed(ActionEvent e) {
//				numberToLaunch = i;
//			}
//		});
//		group.add(b);
//	}
//

	private void addTomenu(final Action a, final JMenu subMenu, final String className, final boolean simpleName) {
		final JMenuItem name = new JMenuItem(a);
		name.setText(simpleName ? MadkitClassLoader.getClassSimpleName(className) : className);
		name.setAccelerator(null);
		name.setActionCommand(className);
		subMenu.add(name);
	}

	public void update() {
		if(! myAgent.isAlive())
			return;
		removeAll();
		final Set<String> classesToLaunch = MadkitClassLoader.getAllAgentClasses();
		if(filter != null)
			for (Iterator<String> iterator = classesToLaunch.iterator(); iterator.hasNext();) {
				if(! filter.accept(iterator.next()))
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
			for (final String cName : classesToLaunch) {
				String pck = MadkitClassLoader.getClassPackageName(cName);
				if (pck != null) {
					if (pck.equals(pckName)) {
						addTomenu(myAction, subMenu, cName, true);
					}
					else {
						pckName = pck;
						subMenu = new JMenu(pck);
						add(subMenu);
						addTomenu(myAction, subMenu, cName, true);
					}
				}
				else{
					addTomenu(myAction, this, cName, false);
				}
			}
		}
//		ButtonGroup group = new ButtonGroup();
//		for (int i = 1; i <= 1000000; i*=10) {
//			addLaunchingNumber(group,i);
//		}
		
	}

}
