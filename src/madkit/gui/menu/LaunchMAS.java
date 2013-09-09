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

import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.KernelAction;
import madkit.gui.MASModel;
import madkit.gui.SwingUtil;
import madkit.i18n.Words;
import madkit.kernel.AbstractAgent;
import madkit.kernel.MadkitClassLoader;

/**
 * This class builds a {@link JMenu} containing all the 
 * MAS which have been found on the class path, so that they can be
 * individually launched according to their configuration.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.16
 * @version 0.9
 * 
 */
public class LaunchMAS extends ClassPathSensitiveMenu {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -3716696545105934911L;
	final private AbstractAgent myAgent;

	/**
	 * Builds a new menu.
	 * @param agent the agent according 
	 * to which this menu should be created, i.e. the
	 * agent that will be responsible of the launch.
	 */
	public LaunchMAS(final AbstractAgent agent) {
		super(Words.MAS.toString());
		setMnemonic(KeyEvent.VK_S);
		myAgent = agent;
		update();
	}

	private void addTomenu(Action a, JMenu subMenu, MASModel demo) {
		JMenuItem name = new JMenuItem(a);
		String displayedName = demo.getName();
		name.setText(displayedName);
		name.setIcon(SwingUtil.MADKIT_LOGO_SMALL);
		name.setToolTipText(demo.toString());
		name.setAccelerator(null);
		name.setActionCommand(displayedName);
		subMenu.add(name);
	}

	public void update() {
		if(! myAgent.isAlive())
			return;
		removeAll();
		for(final MASModel dm : MadkitClassLoader.getAvailableConfigurations()){
			addTomenu(KernelAction.LAUNCH_MAS.getActionFor(myAgent, dm),this,dm);
		}
		setVisible(getItemCount() != 0);
	}

}
