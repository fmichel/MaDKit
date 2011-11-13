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
package madkit.gui.toolbars;

import java.awt.Component;
import java.awt.Image;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import madkit.gui.actions.MadkitAction;
import madkit.kernel.AbstractAgent;

/**
 * An out of the box toolbar for MadKit based applications.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class MadkitToolBar extends JToolBar {// TODO i18n

	/**
	 * 
	 */
	private static final long serialVersionUID = -700298646422969523L;

	/**
	 * Creates a toolbar featuring: 
	 * {@link MadkitAction#LAUNCH_NETWORK}, 
	 * {@link MadkitAction#STOP_NETWORK}, 
	 * {@link MadkitAction#EXIT}, 
	 * {@link MadkitAction#ICONIFY_ALL}, 
	 * {@link MadkitAction#DEICONIFY_ALL}, 
	 * {@link MadkitAction#RESTART}, 
	 * {@link MadkitAction#LOAD_LOCAL_DEMOS}, 
	 * 
	 * @param agent the agent for which this menu is created
	 */
	public MadkitToolBar(final AbstractAgent agent) {
		super("MadKit");
		MadkitAction.addAllActionsTo(this, agent);
		for (final Component c : getComponents()) {
			if (c instanceof AbstractButton) {
				final ImageIcon i = (ImageIcon) ((AbstractButton) c).getIcon();
				if (i != null) {
					i.setImage(i.getImage().getScaledInstance(20, 20, Image.SCALE_SMOOTH));
				}
			}
		}
	}
}
