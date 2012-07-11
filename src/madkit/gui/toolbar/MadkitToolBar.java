/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.gui.toolbar;

import java.awt.Component;
import java.awt.Image;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JToolBar;

import madkit.action.GUIManagerAction;
import madkit.action.KernelAction;
import madkit.kernel.AbstractAgent;

/**
 * An out of the box toolbar for MaDKit based applications.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */
public class MadkitToolBar extends JToolBar {// TODO i18n

	/**
	 * 
	 */
	private static final long serialVersionUID = -700298646422969523L;

	/**
	 * Creates a {@link JToolBar} featuring: 
	 * {@link KernelAction#EXIT}, 
	 * {@link KernelAction#COPY}, 
	 * {@link KernelAction#RESTART}, 
	 * {@link KernelAction#LAUNCH_NETWORK}, 
	 * {@link KernelAction#STOP_NETWORK}, 
	 * {@link GUIManagerAction#ICONIFY_ALL}, 
	 * {@link GUIManagerAction#DEICONIFY_ALL}, 
	 * {@link KernelAction#LOAD_LOCAL_DEMOS}, 
	 * 
	 * @param agent the agent for which this menu is created
	 */
	public MadkitToolBar(final AbstractAgent agent) {
		super("MaDKit");
//		MadkitAction.addAllActionsTo(this, agent);
		KernelAction.addAllActionsTo(this, agent);
		GUIManagerAction.addAllActionsTo(this, agent);
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
