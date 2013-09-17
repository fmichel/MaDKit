/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.menu;

import java.awt.event.KeyEvent;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import madkit.action.GlobalAction;
import madkit.kernel.MadkitClassLoader;

/**
 * This class builds a {@link JMenu} containing all the 
 * agent classes containing a main method
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.1
 * @version 0.9
 * 
 */
public class LaunchMain extends ClassPathSensitiveMenu {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 752058124107737762L;

	/**
	 * Builds a new menu.
	 * 
	 * @param title the title to use
	 */
	public LaunchMain(final String title) {
		super(title);
		setMnemonic(KeyEvent.VK_A);
		update();
	}

	@Override
	public void update() {
		removeAll();
		for (final String string : MadkitClassLoader.getAgentsWithMain()) {
			JMenuItem name = new JMenuItem(GlobalAction.LAUNCH_MAIN);
			name.setActionCommand(string);
			name.setText(string);
			add(name);
		}
		setVisible(getItemCount() != 0);
	}

}
