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
import java.awt.event.KeyEvent;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import javax.swing.AbstractButton;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import madkit.action.ActionInfo;
import madkit.action.AgentAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentLogger;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.91
 * 
 */
public class AgentLogLevelMenu extends JMenu{
	

	private static final long serialVersionUID = -5402608797586593530L;

	final private static Map<AbstractAgent,AgentLogLevelMenu> menus = new HashMap<AbstractAgent, AgentLogLevelMenu>(); 
	
	final private static Level[] logLevels = {Level.OFF,Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST, Level.ALL};

	final private static String lvlShortDesc;
	final private static String lvlLonDesc;
	final private static String warningShortDesc;
	final private static String warningLonDesc;
	final private static ImageIcon lvlIcon;
	final private static ImageIcon wIcon;
	
	static{
		ActionInfo info = AgentAction.LOG_LEVEL.getActionInfo();
		lvlShortDesc = info.getName();
		lvlLonDesc = info.getShortDescription();
		lvlIcon = info.getSmallIcon();
		info = AgentAction.WARNING_LOG_LEVEL.getActionInfo();
		warningShortDesc = info.getName();
		warningLonDesc = info.getShortDescription();
		wIcon = info.getSmallIcon();
	}

	final private AbstractAgent myAgent;
	final private ButtonGroup logGroup;
	final private ButtonGroup warningGroup;
//	final private static ImageIcon logIcon = MadkitAction.AGENT_LOG_LEVEL);
//	final private static ImageIcon wIcon = madkit.gui.GUIToolkit.getMadkitImageIcon(MadkitAction.AGENT_WARNING_LOG_LEVEL);
	
	public AgentLogLevelMenu(final AbstractAgent agent){
		super("Logging");
		setMnemonic(KeyEvent.VK_L);

		myAgent = agent;
		
		JMenu logLevelMenu = new JMenu(lvlShortDesc);
		logLevelMenu.setIcon(lvlIcon);
		logLevelMenu.setToolTipText(lvlLonDesc);
		
		JMenu warningLogLevelMenu = new JMenu(warningShortDesc);
		warningLogLevelMenu.setIcon(wIcon);
		warningLogLevelMenu.setToolTipText(warningLonDesc);
		
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
		update();
		menus.put(myAgent, this);
		
	}
	
	public void update() {
//		if (myAgent.isAlive()) {
			final AgentLogger logger = myAgent.getLogger();
			updateButtonGroup(logGroup, logger.getLevel());
			updateButtonGroup(warningGroup, logger.getWarningLogLevel());
//		}
	}
	
	public static void update(AbstractAgent agent){
		AgentLogLevelMenu menu = menus.get(agent);
		if(menu != null){
			menu.update();
		}
	}
	
	/**
	 * @param logGroup 
	 * @param logLevel
	 */
	private void updateButtonGroup(ButtonGroup logGroup, Level logLevel) {
		for (Enumeration<AbstractButton> buttons = logGroup.getElements();buttons.hasMoreElements();) {
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
	
	//TODO remove agent on dispose


}
