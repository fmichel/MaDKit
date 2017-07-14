/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
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
import javax.swing.Action;
import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JRadioButtonMenuItem;

import madkit.action.ActionInfo;
import madkit.action.LoggingAction;
import madkit.gui.SwingUtil;
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
	

	final private static Map<AbstractAgent,AgentLogLevelMenu> menus = new HashMap<>(); 
	
	final private static Level[] logLevels = {Level.OFF,Level.SEVERE,Level.WARNING,Level.INFO,Level.CONFIG,Level.FINE,Level.FINER,Level.FINEST, Level.ALL};

	final private AbstractAgent myAgent;
	final private ButtonGroup logGroup;
//	final private ButtonGroup warningGroup;

	private Action cgrWarningsAction;
	
	/**
	 * Builds a menu containing all the log levels which
	 * could be set on an agent. 
	 */
	public AgentLogLevelMenu(final AbstractAgent agent){
		super("Logging");
		setMnemonic(KeyEvent.VK_L);

		myAgent = agent;
		
		ActionInfo action = LoggingAction.LOG_LEVEL.getActionInfo();
		JMenu logLevelMenu = new JMenu(action.getName());
		logLevelMenu.setIcon(action.getSmallIcon());
		logLevelMenu.setToolTipText(action.getShortDescription());
		
		logGroup = new ButtonGroup();
		
		add(logLevelMenu);
		
		final ActionListener setLogLevelListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				myAgent.getLogger().setLevel(Level.parse( e.getActionCommand()));
//				SwingUtil.UI_PREFERENCES.put(agentName+"LL",e.getActionCommand());
			}
		};

		SwingUtil.addBooleanActionTo(this,myAgent.getLogger().getEnableCGRWarningsAction());

		for(final Level l : logLevels){
			JRadioButtonMenuItem logItem = new JRadioButtonMenuItem(l.getLocalizedName());
			initMenuItem(logItem,setLogLevelListener,l.toString(),logGroup,logLevelMenu);
		}
//		myAgent.setLogLevel(Level.parse(SwingUtil.UI_PREFERENCES.get(agentName+"LL",logger.getLevel().toString())));
		update();
		if (myAgent.hasGUI()) {//TODO need mvc here
			menus.put(myAgent, this);
		}
		
	}
	
	private void update() {
			updateLogLevelGroup(myAgent.getLogger().getLevel());
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
	 * @param logLevel
	 */
	private void updateLogLevelGroup(final Level logLevel) {
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

	public static void remove(AbstractAgent abstractAgent) {
		menus.remove(abstractAgent);
	}
	
	//TODO remove agent on dispose


}
