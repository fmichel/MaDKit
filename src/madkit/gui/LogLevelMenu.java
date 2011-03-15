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
import java.util.Enumeration;
import java.util.Iterator;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentLogger;
import madkit.kernel.Madkit;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class LogLevelMenu extends JMenu implements AgentUIComponent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5402608797586593530L;
	static AbstractAction setLogLevelAction;
	static AbstractAction setWarningLogLevelAction;
	static Level[] logLevels = {Level.OFF,Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST, Level.ALL};
	private AbstractAgent myAgent;
	private ButtonGroup logGroup;
	private ButtonGroup warningGroup;
	
	LogLevelMenu(final AbstractAgent agent){
		super("Logging");
		myAgent = agent;
		
		JMenu logLevelMenu = new JMenu("Log level");
		logLevelMenu.setIcon(madkit.gui.Utils.getMadkitImageIcon("agent.logLevel.s"));
		logLevelMenu.setToolTipText("the agents's current log level");
		
		JMenu warningLogLevelMenu = new JMenu("Warning log level");
		warningLogLevelMenu.setIcon(madkit.gui.Utils.getMadkitImageIcon("agent.warningLogLevel.s"));
		warningLogLevelMenu.setToolTipText("the agent's log level above which warnings are displayed");
		
		logGroup = new ButtonGroup();
		warningGroup = new ButtonGroup();
		
		add(logLevelMenu);
		add(warningLogLevelMenu);
		
		ActionListener setLogLevelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				agent.setLogLevel(Level.parse( ((AbstractButton)e.getSource()).getActionCommand()));
			}
		};
		ActionListener setWarningLogLevelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				agent.setLogLevel((agent.getLogger() != null) ? Level.INFO : agent.getLogger().getLevel(),Level.parse( ((AbstractButton)e.getSource()).getActionCommand()));
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
		AgentLogger logger = myAgent.getLogger();
		Level currentLogLevel = logger == null ? Level.OFF : logger.getLevel();
		Level currentWarningLogLevel = logger == null ? Level.parse(myAgent.getMadkitProperty(Madkit.warningLogLevel)) : logger.getWarningLogLevel();
		updateButtonGroup(logGroup, currentLogLevel);
		updateButtonGroup(warningGroup, currentWarningLogLevel);
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

	@Override
	public AbstractAgent getAgent() {
		return myAgent;
	}

	public static Action getSetLogLevelAction(){
		if(setLogLevelAction == null){
		setLogLevelAction = new AbstractAction("SetLogLevelAction") {
			@Override
			public void actionPerformed(ActionEvent e) {
				Component c = (Component) e.getSource();
				JRadioButtonMenuItem item = (JRadioButtonMenuItem) c;
				while(! (c instanceof AgentUIComponent) && c != null){
					c = c.getParent();
				}
				((AgentUIComponent)c).getAgent().setLogLevel(Level.parse(item.getActionCommand()));
			}
		};
		}
//		ImageIcon logIcon = new ImageIcon(setLogLevelAction.getClass().getResource("/images/agent/warningLogLevel16.gif"));
		return setLogLevelAction;
	}

}
