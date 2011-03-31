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
public class AgentMenu extends JMenu implements AgentUIComponent{//TODO i18n

	final private AbstractAgent myAgent;
	
	AgentMenu(final AbstractAgent agent){
		super("Agent");
		setMnemonic(KeyEvent.VK_A);
		myAgent = agent;
		add(getRelaunchAction(agent));
		add(getLaunchAnotherAction(agent));
		Action a = getReloadAndRelaunchAction(agent);
		if (a != null) {
			add(getReloadAndRelaunchAction(agent));
		}
		add(getKillAction(agent));
		add(getExitMadkitAction(agent));
	}
	
	public static Action getExitMadkitAction(final AbstractAgent a){
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						a.requestRole(LOCAL_COMMUNITY, SYSTEM_GROUP, a.getName());
						a.sendMessage(LOCAL_COMMUNITY, SYSTEM_GROUP, KERNEL_ROLE, new KernelMessage(OperationCode.SHUTDOWN_NOW, (Object)null));
					}
				});
			}
		};
		Utils.initAction(action, 
				"Kill all agents and exit MadKit", 
				"Kill all agents and exit MadKit",
				"ExitMadKit",
				"ExitMadKit",
				KeyEvent.VK_E,
				"madkit.exit",
				KeyStroke.getKeyStroke(KeyEvent.VK_E,KeyEvent.CTRL_MASK),
				true);
		return action;
	}
	
	public static Action getRelaunchAction(final AbstractAgent a){
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						a.launchAgent(a.getClass().getName(),true);
						a.killAgent(a);
					}
				});
			}
		};
		Utils.initAction(action, 
				"relaunch this agent", 
				"relaunch this agent",
				"Relaunch",
				"Relaunch",
				KeyEvent.VK_L,
				"agent.relaunch",
				KeyStroke.getKeyStroke(KeyEvent.VK_L,KeyEvent.CTRL_MASK),
				true);
		return action;
	}

	public static Action getLaunchAnotherAction(final AbstractAgent a){
		AbstractAction action = new AbstractAction("launchAnother") {
			@Override
			public void actionPerformed(ActionEvent e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						a.launchAgent(a.getClass().getName(),0,true);
					}
				});
			}
		};
		Utils.initAction(action, 
				"Launch another instance of this agent", 
				"Launch another instance of this agent", 
				"launchAnother",
				"launchAnother",
				KeyEvent.VK_O,
				"agent.launchAnother",
				KeyStroke.getKeyStroke(KeyEvent.VK_O,KeyEvent.CTRL_MASK),
				true);
		return action;
	}

	public static Action getReloadAndRelaunchAction(final AbstractAgent a){
		String className = a.getClass().getName();
		if(className.contains("madkit.kernel") || className.contains("madkit.gui"))
			return null;
		AbstractAction action = new AbstractAction("Reload") {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
							try {
								a.reloadAgentClass(a.getClass().getName());
							} catch (ClassNotFoundException e) {
								e.printStackTrace();//TODO log this but should not happen
							}
							a.launchAgent(a.getClass().getName(), true);
							if (a.getState() != AbstractAgent.State.TERMINATED) {
								a.killAgent(a);
								//						} catch (IllegalAccessError e) {
								//							e.printStackTrace();
								//							JOptionPane.showMessageDialog(null, "Reload is for now not functionnal on this agent (probably embedding inner classes) Sorry...", "MadKit apologies", JOptionPane.WARNING_MESSAGE);
								//						}
							}
					}
				});
			}
		};
		Utils.initAction(action, 
				"Reload class and then relaunch agent", 
				"Reload class and then relaunch agent", 
				"Reload",
				"Reload",
				KeyEvent.VK_A,
				"agent.reload",
				KeyStroke.getKeyStroke(KeyEvent.VK_A,KeyEvent.CTRL_MASK),
				true);
		return action;
	}
	
	public static Action getKillAction(final AbstractAgent a){
		AbstractAction action = new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
							a.killAgent(a);
					}
				});
			}
		};
		Utils.initAction(action, 
				"Kill this agent", 
				"Kill this agent", 
				"Kill",
				"Kill",
				KeyEvent.VK_K,
				"agent.kill",
				KeyStroke.getKeyStroke(KeyEvent.VK_K,KeyEvent.CTRL_MASK),
				true);
		return action;
	}

	/**
	 * @see madkit.gui.AgentUIComponent#updateAgentUI()
	 */
	@Override
	public void updateAgentUI() {
	}

	@Override
	public AbstractAgent getAgent() {
		return myAgent;
	}

}
