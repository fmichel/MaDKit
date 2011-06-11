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
package madkit.gui.menus;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Enumeration;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import madkit.gui.AgentUIComponent;
import madkit.gui.actions.AgentAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentLogger;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class AgentLogLevelMenu extends JMenu implements AgentUIComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5402608797586593530L;
	final private static Level[] logLevels = {Level.OFF,Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST, Level.ALL};
	final private AbstractAgent myAgent;
	final private ButtonGroup logGroup;
	final private ButtonGroup warningGroup;
	final private static String[] lvlCodes = AgentAction.AGENT_LOG_LEVEL.toString().split(";");
	final private static String[] warningCodes = AgentAction.AGENT_WARNING_LOG_LEVEL.toString().split(";");
//	final private static ImageIcon logIcon = MadkitActions.AGENT_LOG_LEVEL);
//	final private static ImageIcon wIcon = madkit.gui.GUIToolkit.getMadkitImageIcon(MadkitActions.AGENT_WARNING_LOG_LEVEL);
	
	public AgentLogLevelMenu(final AbstractAgent agent){
		super("Logging");
		myAgent = agent;
		
		JMenu logLevelMenu = new JMenu(lvlCodes[0]);
		logLevelMenu.setIcon(AgentAction.AGENT_LOG_LEVEL.getImageIcon());
		logLevelMenu.setToolTipText(lvlCodes[1]);
		
		JMenu warningLogLevelMenu = new JMenu(warningCodes[0]);
		warningLogLevelMenu.setIcon(AgentAction.AGENT_WARNING_LOG_LEVEL.getImageIcon());
		warningLogLevelMenu.setToolTipText(warningCodes[1]);
		
		logGroup = new ButtonGroup();
		warningGroup = new ButtonGroup();
		
		add(logLevelMenu);
		add(warningLogLevelMenu);
		
		ActionListener setLogLevelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myAgent.setLogLevel(Level.parse( e.getActionCommand()));
			}
		};
		ActionListener setWarningLogLevelListener = new ActionListener() {
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
		updateAgentUI();
		
	}
	
	/**
	 * @see madkit.gui.AgentUIComponent#updateAgentUI()
	 */
	@Override
	public void updateAgentUI() {
//		if (myAgent.isAlive()) {
			AgentLogger logger = myAgent.getLogger();
			Level currentLogLevel = logger.getLevel();
			Level currentWarningLogLevel = logger.getWarningLogLevel();
			updateButtonGroup(logGroup, currentLogLevel);
			updateButtonGroup(warningGroup, currentWarningLogLevel);
//		}
	}

	/**
	 * @param logGroup 
	 * @param logLevel
	 */
	private void updateButtonGroup(ButtonGroup logGroup, Level logLevel) {
		for (Enumeration<AbstractButton> buttons = logGroup.getElements();buttons.hasMoreElements();) {
			AbstractButton button = buttons.nextElement();
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

}
