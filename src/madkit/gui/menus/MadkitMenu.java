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

import java.awt.event.KeyEvent;

import javax.swing.JMenu;

import madkit.gui.actions.MadkitActions;
import madkit.kernel.AbstractAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class MadkitMenu extends JMenu {//TODO i18n

	private static final long serialVersionUID = 6177193453649323680L;

	public MadkitMenu(final AbstractAgent agent){
		super("MadKit");
		setMnemonic(KeyEvent.VK_M);
		add(MadkitActions.MADKIT_LAUNCH_NETWORK.getAction(agent));
		add(MadkitActions.MADKIT_STOP_NETWORK.getAction(agent));
		add(MadkitActions.MADKIT_EXIT_ACTION.getAction(agent));
		add(MadkitActions.MADKIT_KILL_AGENTS.getAction(agent));
		add(MadkitActions.MADKIT_LOAD_JAR_FILE.getAction(agent));
	}
}
