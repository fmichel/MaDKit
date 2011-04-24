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
import java.awt.Image;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import madkit.kernel.AbstractAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class MadkitToolBar extends JToolBar {// TODO i18n

	public MadkitToolBar(final AbstractAgent agent) {
		super("MadKit");
		add(MadkitActions.MADKIT_LAUNCH_NETWORK.getAction(agent));
		add(MadkitActions.MADKIT_STOP_NETWORK.getAction(agent));
		add(MadkitActions.MADKIT_EXIT_ACTION.getAction(agent));
		addSeparator();
		add(MadkitActions.MADKIT_ICONIFY_ALL.getAction(agent));
		add(MadkitActions.MADKIT_DEICONIFY_ALL.getAction(agent));
		add(MadkitActions.MADKIT_RESTART.getAction(agent));
		add(MadkitActions.MADKIT_CLONE.getAction(agent));
		add(MadkitActions.CONNECT_WEB_REPO.getAction(agent));
		for (Component c : getComponents()) {
			if (c instanceof AbstractButton) {
				ImageIcon i = (ImageIcon) ((AbstractButton) c).getIcon();
				if (i != null) {
					i.setImage(i.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
				}
			}
		}
	}
}
