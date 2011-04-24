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

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenu;
import javax.swing.KeyStroke;

import madkit.kernel.AbstractAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
public class AgentMenu extends JMenu {// TODO i18n

//	final private AbstractAgent myAgent;

	AgentMenu(final AbstractAgent agent) {
		super("Agent");
		setMnemonic(KeyEvent.VK_A);
//		myAgent = agent;
		add(AgentAction.AGENT_RELAUNCH.getAction(agent));
		add(AgentAction.AGENT_LAUNCH_ANOTHER.getAction(agent));
		Action a = AgentAction.AGENT_RELOAD.getAction(agent);
		if (a != null) {
			add(a);
		}
		add(AgentAction.AGENT_KILL.getAction(agent));
	}
}
