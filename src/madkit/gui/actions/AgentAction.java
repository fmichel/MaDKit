/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.actions;

import static java.awt.event.KeyEvent.VK_E;
import static java.awt.event.KeyEvent.VK_H;
import static java.awt.event.KeyEvent.VK_J;
import static java.awt.event.KeyEvent.VK_O;
import static java.awt.event.KeyEvent.VK_Z;

import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import madkit.i18n.I18nUtilities;
import madkit.kernel.AbstractAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public enum AgentAction implements MadkitGUIAction{

	AGENT_RELOAD(null,VK_H),
	AGENT_KILL(null,VK_Z),
	AGENT_LAUNCH_ANOTHER(null,VK_O),
	AGENT_RELAUNCH(null,VK_J),
	AGENT_LOG_LEVEL(new ImageIcon(AgentAction.class.getResource("images/agent/logs.png")),0),
	AGENT_WARNING_LOG_LEVEL(new ImageIcon(AgentAction.class.getResource("images/agent/warningLogLevel.gif")),0),
	AGENT_LAUNCH_AGENT(null,VK_E);
	
	final private ImageIcon imageIcon;

	final static ResourceBundle messages = I18nUtilities.getResourceBundle(MadkitGUIAction.class.getSimpleName());

	public ImageIcon getImageIcon() {
		return imageIcon;
	}

	public int getKeyEvent() {
		return keyEvent;
	}

	final private int keyEvent;
	
	private AgentAction(ImageIcon ii, int keyEvent){
		imageIcon = ii;
		this.keyEvent = keyEvent;
	}
	
	public Action getAction(final AbstractAgent agent){
		Action a;
		switch (this) {
		case AGENT_LOG_LEVEL:
		case AGENT_WARNING_LOG_LEVEL:
		case AGENT_RELOAD:
			a = getReloadAndRelaunchAction(agent);
			break;
		case AGENT_KILL:
			a = getKillAction(agent);
			break;
		case AGENT_LAUNCH_AGENT:
		case AGENT_LAUNCH_ANOTHER:
			a = getLaunchAnotherAction(agent);
			break;
		case AGENT_RELAUNCH:
			a = getRelaunchAction(agent);
			break;
		default:
			throw new AssertionError(this);
		}
		if(a == null)
			return null;
		return Actions.initAction(this, a);
	}

	private Action getRelaunchAction(final AbstractAgent agent) {
		if (! Actions.hasDefaultConstructor(agent))
			return null;
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				agent.launchAgent(agent.getClass().getName(), 0, true);
				Actions.selfKill(agent);
			}
		};
	}
	private Action getLaunchAnotherAction(final AbstractAgent agent) {
		if (! Actions.hasDefaultConstructor(agent))
			return null;
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				agent.launchAgent(agent.getClass().getName(), 0, true);
			}
		};
	}

	private Action getKillAction(final AbstractAgent a) {
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				Actions.selfKill(a);
			}
		};
	}

	private Action getReloadAndRelaunchAction(final AbstractAgent agent) {
		String className = agent.getClass().getName();
		if (! Actions.hasDefaultConstructor(agent) || className.contains("madkit.kernel") || className.contains("madkit.gui"))
			return null;
		return new AbstractAction() {
			@Override
			public void actionPerformed(ActionEvent e) {
				this.setEnabled(false);
				try {
					agent.reloadAgentClass(agent.getClass().getName());
				} catch (ClassNotFoundException ex) {
					ex.printStackTrace();// TODO log this but should not happen
				}
				agent.launchAgent(agent.getClass().getName(),0, true);
				if (agent.getState() != AbstractAgent.State.TERMINATED) {
					Actions.selfKill(agent);
				}
			}
		};
	}
	
	@Override
	public String toString() {
		return Actions.getDescription(this);
	}
	
}
