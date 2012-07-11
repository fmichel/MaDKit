/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import madkit.action.ActionInfo;
import madkit.action.GUIManagerAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentLogger;

/**
 * An out of the box menu for manipulating the
 * log level of an agent.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @see AgentLogger
 * @version 0.91
 * 
 */
public class AgentLogLevelMenu extends JMenu{
	

	private static final long serialVersionUID = -5402608797586593530L;

	final private static Map<AbstractAgent,AgentLogLevelMenu> menus = new HashMap<AbstractAgent, AgentLogLevelMenu>(); 
	
	final private static Level[] logLevels = {Level.OFF,Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST, Level.ALL};

	final private AbstractAgent myAgent;
	final private ButtonGroup logGroup;
	final private ButtonGroup warningGroup;
	
	/**
	 * Builds a menu containing all the log levels which
	 * could be set on an agent. 
	 */
	public AgentLogLevelMenu(final AbstractAgent agent){
		super("Logging");
		setMnemonic(KeyEvent.VK_L);

		myAgent = agent;
		
		ActionInfo action = GUIManagerAction.LOG_LEVEL.getActionInfo();
		JMenu logLevelMenu = new JMenu(action.getName());
		logLevelMenu.setIcon(action.getSmallIcon());
		logLevelMenu.setToolTipText(action.getShortDescription());
		
		action = GUIManagerAction.WARNING_LOG_LEVEL.getActionInfo();
		JMenu warningLogLevelMenu = new JMenu(action.getName());
		warningLogLevelMenu.setIcon(action.getSmallIcon());
		warningLogLevelMenu.setToolTipText(action.getShortDescription());
		
		logGroup = new ButtonGroup();
		warningGroup = new ButtonGroup();
		
		add(logLevelMenu);
		add(warningLogLevelMenu);
		
		final ActionListener setLogLevelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myAgent.setLogLevel(Level.parse( e.getActionCommand()));
			}
		};
		
		final ActionListener setWarningLogLevelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myAgent.getLogger().setWarningLogLevel(Level.parse( e.getActionCommand()));
			}
		};
		
		for(final Level l : logLevels){
			JRadioButtonMenuItem logItem = new JRadioButtonMenuItem(l.getLocalizedName());
			JRadioButtonMenuItem warningItem = new JRadioButtonMenuItem(l.getLocalizedName());
			initMenuItem(logItem,setLogLevelListener,l.toString(),logGroup,logLevelMenu);
			initMenuItem(warningItem,setWarningLogLevelListener,l.toString(),warningGroup,warningLogLevelMenu);
		}
		update();
		if (myAgent.hasGUI()) {//TODO need mvc here
			menus.put(myAgent, this);
		}
		
	}
	
	private void update() {
//		if (myAgent.isAlive()) {
			final AgentLogger logger = myAgent.getLogger();
			updateButtonGroup(logGroup, logger.getLevel());
			updateButtonGroup(warningGroup, logger.getWarningLogLevel());
//		}
	}
	
	/**
	 * Update the menu of this agent
	 * 
	 * @param agent
	 */
	public static void update(final AbstractAgent agent){
		final AgentLogLevelMenu menu = menus.get(agent);
		if(menu != null){
			menu.update();
		}
	}
	
	/**
	 * @param group 
	 * @param logLevel
	 */
	private void updateButtonGroup(final ButtonGroup group, final Level logLevel) {
		for (Enumeration<AbstractButton> buttons = group.getElements();buttons.hasMoreElements();) {
			final AbstractButton button = buttons.nextElement();
			if(button.getActionCommand().equals(logLevel.toString())){
				button.setSelected(true);
				return;
			}
		}
	}

	/**
	 * @param logItem
	 * @param listener 
	 */
	private void initMenuItem(JRadioButtonMenuItem logItem,ActionListener listener, String actionCommand, ButtonGroup group, JMenu menu) {
		menu.add(logItem);
		logItem.setActionCommand(actionCommand);
		logItem.addActionListener(listener);
		group.add(logItem);
	}

	public static void remove(AbstractAgent abstractAgent) {
		menus.remove(abstractAgent);
	}
	
	//TODO remove agent on dispose


}
