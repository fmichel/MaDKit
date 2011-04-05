/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui;

import java.awt.Component;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentLogger;
import madkit.kernel.KernelMessage;
import madkit.kernel.Madkit;
import madkit.kernel.KernelMessage.OperationCode;
import static madkit.kernel.Madkit.Roles.*;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class MadkitMenu extends JMenu {//TODO i18n

	private static final long serialVersionUID = 6177193453649323680L;

	static Action getExitMadkitAction(final AbstractAgent a){
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						a.sendMessage(LOCAL_COMMUNITY, SYSTEM_GROUP, KERNEL_ROLE, new KernelMessage(OperationCode.SHUTDOWN_NOW, (Object)null));
						a.killAgent(a);
					}
				});
			}
		};
		Utils.initAction(action, 
				"Kill all agents and exit MadKit kernel "+a.getKernelAddress(), 
				"Kill all agents and exit MadKit kernel "+a.getKernelAddress(),
				"Exit MadKit",
				"Exit MadKit",
				KeyEvent.VK_E,
				"madkit.exit",
				KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.CTRL_MASK),
				true);
		return action;
	}

	static Action getLaunchNetworkAction(final AbstractAgent a){
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						if(a.getAgentsWithRole(LOCAL_COMMUNITY, NETWORK_GROUP, NETWORK_ROLE) == null){
							a.launchAgent(a.getMadkitProperty("networkAgent"));
//							this.setEnabled(false);
						}
					}
				});
			}
		};
		Utils.initAction(action, 
				"Launch network on "+a.getKernelAddress(), 
				"Launch network on "+a.getKernelAddress(), 
				"Launch network",
				"Launch network",
				KeyEvent.VK_N,
				"madkit.network",
				KeyStroke.getKeyStroke(KeyEvent.VK_N,KeyEvent.CTRL_MASK),
				true);
		if(a.getAgentsWithRole(LOCAL_COMMUNITY, NETWORK_GROUP, NETWORK_ROLE) == null)
			action.setEnabled(false);
		return action;
	}

	static Action getStopNetworkAction(final AbstractAgent a){
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
//						if(a.getAgentsWithRole(LOCAL_COMMUNITY, NETWORK_GROUP, NETWORK_ROLE) == null)
//							a.launchAgent(a.getMadkitProperty("networkAgent"));
					}
				});
			}
		};
		Utils.initAction(action, 
				"Stop network on "+a.getKernelAddress(), 
				"Stop network on "+a.getKernelAddress(), 
				"Stop network",
				"Stop network",
				KeyEvent.VK_S,
				"madkit.network",//TODO icon
				KeyStroke.getKeyStroke(KeyEvent.VK_N,KeyEvent.ALT_MASK),
				true);
		if(a.getAgentsWithRole(LOCAL_COMMUNITY, NETWORK_GROUP, NETWORK_ROLE) == null)
			action.setEnabled(false);
		return action;
	}


	public MadkitMenu(final AbstractAgent agent){
		super("MadKit");
		setMnemonic(KeyEvent.VK_M);
		add(getLaunchNetworkAction(agent));
		add(getStopNetworkAction(agent));
		add(getExitMadkitAction(agent));
	}
}
