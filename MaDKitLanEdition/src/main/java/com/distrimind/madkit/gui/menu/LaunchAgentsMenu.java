/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Set;

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.distrimind.madkit.action.AgentAction;
import com.distrimind.madkit.action.MDKAbstractAction;
import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.MadkitClassLoader;
import com.distrimind.madkit.kernel.NetworkAgent;

/**
 * This class builds a {@link JMenu} containing all the agents which are on the
 * class path, so that they can be individually launched.
 * 
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
	// final static private Set<LaunchAgentsMenu> menus = new HashSet<>();
	final private Action myAction;
	final private AbstractAgent myAgent;
	private final AgentClassFilter filter;
	// protected final int numberToLaunch;

	/**
	 * Builds a new menu, "Agents", containing all the agents available on the class
	 * path.
	 * 
	 * @param agent
	 *            the agent according to which this menu should be created, i.e. the
	 *            agent that will be responsible of the launch.
	 */
	public LaunchAgentsMenu(final AbstractAgent agent) {
		this(agent, "Agents", null);
	}

	/**
	 * Builds a new menu containing all the agents available on the class path,
	 * except madkit.kernel agents.
	 * 
	 * @param agent
	 *            the agent according to which this menu should be created, i.e. the
	 *            agent that will be responsible of the launch.
	 * @param menuName
	 *            the name of the menu
	 */
	public LaunchAgentsMenu(final AbstractAgent agent, final String menuName) {
		this(agent, menuName, null);
	}

	/**
	 * Builds a new launching menu containing all the agents available on the class
	 * path.
	 * 
	 * @param agent
	 *            the agent according to which this menu should be created, i.e. the
	 *            agent that will be responsible of the launch.
	 * @param menuName
	 *            the name of the menu
	 * @param filter
	 *            help filtering which classes should be included
	 */
	public LaunchAgentsMenu(final AbstractAgent agent, String menuName, AgentClassFilter filter) {
		super(menuName);
		this.filter = filter;
		setMnemonic(KeyEvent.VK_G);
		myAgent = agent;
		myAction = new MDKAbstractAction(AgentAction.LAUNCH_AGENT.getActionInfo()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 6530886642947530268L;

			@Override
			public void actionPerformed(ActionEvent e) {
				agent.launchAgent(e.getActionCommand(), 0, true);
			}
		};
		update();
	}

	// private void addLaunchingNumber(ButtonGroup group, final int i) {//TODO
	// final JRadioButton b = new JRadioButton(Words.LAUNCH.toString()+ " : "+i);
	// add(b);
	// b.addActionListener(new ActionListener() {
	// @Override
	// public void actionPerformed(ActionEvent e) {
	// numberToLaunch = i;
	// }
	// });
	// group.add(b);
	// }
	//

	private void addTomenu(final Action a, final JMenu subMenu, final String className, final boolean simpleName) {
		final JMenuItem name = new JMenuItem(a);
		name.setText(simpleName ? MadkitClassLoader.getClassSimpleName(className) : className);
		name.setAccelerator(null);
		name.setActionCommand(className);
		subMenu.add(name);
	}

	public void update() {
		if (!myAgent.isAlive())
			return;
		removeAll();
		final Set<String> classesToLaunch = MadkitClassLoader.getAllAgentClasses();
		for (Iterator<String> iterator = classesToLaunch.iterator(); iterator.hasNext();) {
			String agentClass = iterator.next();
			if (agentClass.equals(NetworkAgent.class.getName()) || (filter != null && !filter.accept(agentClass)))
				iterator.remove();
		}

		if (filter != null) {
		}
		if (classesToLaunch.size() < 20) {
			for (String string : classesToLaunch) {
				addTomenu(myAction, this, string, false);

			}
		} else {
			String pckName = null;
			JMenu subMenu = null;
			for (final String cName : classesToLaunch) {
				String pck = MadkitClassLoader.getClassPackageName(cName);
				if (pck != null) {
					if (pck.equals(pckName)) {
						addTomenu(myAction, subMenu, cName, true);
					} else {
						pckName = pck;
						subMenu = new JMenu(pck);
						add(subMenu);
						addTomenu(myAction, subMenu, cName, true);
					}
				} else {
					addTomenu(myAction, this, cName, false);
				}
			}
		}
		// ButtonGroup group = new ButtonGroup();
		// for (int i = 1; i <= 1000000; i*=10) {
		// addLaunchingNumber(group,i);
		// }

	}

}
