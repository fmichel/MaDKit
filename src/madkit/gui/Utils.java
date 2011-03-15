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

import static madkit.kernel.Scheduler.State.RUNNING;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import madkit.kernel.AbstractAgent;
import madkit.kernel.KernelMessage;
import madkit.kernel.Madkit;
import madkit.kernel.Scheduler;
import madkit.kernel.Madkit.Roles;

/**
 * Class containing static which could used to build agent GUI
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class Utils {
	
	//TODO every actions static. So avoiding useless instances
	static ConcurrentMap<AbstractAgent,List<AgentUIComponent>> agentUIListeners = new ConcurrentHashMap<AbstractAgent, List<AgentUIComponent>>();
	
	static Map<String,ImageIcon> madkitIcons = new HashMap<String,ImageIcon>();
	
	static{
		madkitIcons.put("agent.logLevel.s",new ImageIcon(Utils.class.getResource("images/agent/logs16.png")));
		madkitIcons.put("agent.warningLogLevel.s",new ImageIcon(Utils.class.getResource("images/agent/warningLogLevel16.gif")));

		madkitIcons.put("scheduler.run.s",new ImageIcon(Utils.class.getResource("images/scheduling/run16.png")));
		madkitIcons.put("scheduler.run.b",new ImageIcon(Utils.class.getResource("images/scheduling/run32.png")));
		madkitIcons.put("scheduler.step.s",new ImageIcon(Utils.class.getResource("images/scheduling/step16.png")));
		madkitIcons.put("scheduler.step.b",new ImageIcon(Utils.class.getResource("images/scheduling/step32.png")));
		madkitIcons.put("scheduler.speedUp.s",new ImageIcon(Utils.class.getResource("images/scheduling/speedUp16.png")));
		madkitIcons.put("scheduler.speedDown.s",new ImageIcon(Utils.class.getResource("images/scheduling/speedDown16.png")));
		madkitIcons.put("scheduler.step",new ImageIcon(Utils.class.getResource("images/agent/warningLogLevel16.gif")));
	}
	
	static public void updateAgentUI(AbstractAgent a){
		List<AgentUIComponent> l = agentUIListeners.get(a);
		if(l != null){
			for(AgentUIComponent ui : l){
				ui.updateAgentUI();
			}
		}
	}

	static public JMenu createLogLevelMenu(final AbstractAgent agent){
		LogLevelMenu menu = new LogLevelMenu(agent);
		menu.setMnemonic(KeyEvent.VK_L);
		agentUIListeners.putIfAbsent(agent, new ArrayList<AgentUIComponent>());
		agentUIListeners.get(agent).add(menu);
		return menu;
	}
	
	static public JMenu createLaunchingMenu(final AbstractAgent agent){
		return new AgentMenu(agent);
	}
	
	
	
	
public static void initAction(AbstractAction a, 
		String ShortDescription, 
		String LongDescription, 
		String actionCommand, 
		String name, 
		int mnemonic, 
		String inconCode,
		KeyStroke accelerator,
		boolean selected){
	a.putValue(AbstractAction.SHORT_DESCRIPTION, ShortDescription);
	a.putValue(AbstractAction.LONG_DESCRIPTION, LongDescription);
	ImageIcon bigIcon = getMadkitImageIcon(inconCode+".b");
	if(bigIcon != null)
		a.putValue(AbstractAction.LARGE_ICON_KEY, bigIcon);
	bigIcon = getMadkitImageIcon(inconCode+".s");
	if (bigIcon != null) {
		a.putValue(AbstractAction.SMALL_ICON, bigIcon);
	}
	a.putValue(Action.ACCELERATOR_KEY, accelerator);
	a.putValue(Action.MNEMONIC_KEY, mnemonic);
	a.putValue(Action.ACTION_COMMAND_KEY, actionCommand);
	a.putValue(Action.NAME, name);
	a.putValue(Action.SELECTED_KEY, selected);
}

public static ImageIcon getMadkitImageIcon(String fileCode){
	return madkitIcons.get(fileCode);
}

}