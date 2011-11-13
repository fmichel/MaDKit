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

import java.awt.event.KeyEvent;
import java.lang.reflect.Constructor;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.KeyStroke;

import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.agr.LocalCommunity.Roles;
import madkit.kernel.AbstractAgent;
import madkit.messages.KernelMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.13
 * @version 0.9
 * 
 */
final class Actions {

	static Action initAction(MadkitGUIAction mka, Action a) {
		String[] codes = mka.toString().split(";");
		a.putValue(Action.NAME, codes[0]);
		a.putValue(Action.SHORT_DESCRIPTION, codes.length > 1 ? codes[1] : codes[0]);
		a.putValue(Action.LONG_DESCRIPTION, codes.length > 2 ? codes[2] : a.getValue(Action.SHORT_DESCRIPTION));
		ImageIcon bigIcon = mka.getImageIcon();
		if (bigIcon != null) {
			a.putValue(AbstractAction.LARGE_ICON_KEY, bigIcon);
			if (bigIcon.getIconWidth() > 16) {
				a.putValue(AbstractAction.SMALL_ICON,
						new ImageIcon(bigIcon.getImage().getScaledInstance(16, 16, java.awt.Image.SCALE_SMOOTH)));
			} else {
				a.putValue(AbstractAction.SMALL_ICON, bigIcon);
			}
		}
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(mka.getKeyEvent(), KeyEvent.CTRL_MASK));
		a.putValue(Action.MNEMONIC_KEY, mka.getKeyEvent());
		a.putValue(Action.ACTION_COMMAND_KEY, mka.toString());
		a.putValue(Action.SELECTED_KEY, false);
		return a;
	}

	static boolean hasDefaultConstructor(AbstractAgent agent){
		try {
			for (Constructor<?> c : agent.getClass().getConstructors()) {
				if(c.getParameterTypes().length == 0)
					return true;
			}
		} catch (SecurityException e) {
		}
		return false;
	}

	static String getDescription(MadkitGUIAction mka){
		return AgentAction.messages.getString(mka.name());
	}

	static void selfKill(AbstractAgent agent){
		if (agent.isAlive()) {
			agent.sendMessage(LocalCommunity.NAME, Groups.SYSTEM, Roles.KERNEL, new KernelMessage(MadkitAction.MADKIT_KILL_AGENT,
					agent, 2));
		}
	}

	
}
