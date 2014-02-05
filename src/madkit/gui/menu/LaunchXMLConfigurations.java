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

import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.KernelAction;
import madkit.action.MDKAbstractAction;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.MadkitClassLoader;
import madkit.message.KernelMessage;

/**
 * This class builds a {@link JMenu} containing all the 
 * MDK xml configuration files found on the class path.
 * If checked, a new MaDKit instance will be used for the
 * corresponding configuration.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.2
 * @version 0.9
 * 
 */
public class LaunchXMLConfigurations extends ClassPathSensitiveMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3650744981788324553L;
	private final AbstractAgent	myAgent;

	/**
	 * Builds a new menu.
	 * @param title the title to use
	 */
	public LaunchXMLConfigurations(final AbstractAgent agent, final String title) {
		super(title);
		myAgent = agent;
		setMnemonic(KeyEvent.VK_X);
		update();
	}

	@Override
	public void update() {
		removeAll();
		final JCheckBoxMenuItem cbox = new JCheckBoxMenuItem("+ MaDKit instance");
		final Action a = new MDKAbstractAction(KernelAction.LAUNCH_XML.getActionInfo() ) {
			private static final long	serialVersionUID	= 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if(myAgent.isAlive()){
					myAgent.sendMessage(
						LocalCommunity.NAME, 
						Groups.SYSTEM, 
						Organization.GROUP_MANAGER_ROLE, 
						new KernelMessage(KernelAction.LAUNCH_XML, e.getActionCommand(),cbox.getState()));
				}
			}
		};
		for (final String string : MadkitClassLoader.getXMLConfigurations()) {
			JMenuItem name = new JMenuItem(a);
			name.setActionCommand(string);
			name.setText(string);
			add(name);
		}
		add(cbox);
		setVisible(getItemCount() != 1);
	}

}
