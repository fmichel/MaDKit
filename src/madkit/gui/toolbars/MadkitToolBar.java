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

import madkit.gui.actions.AgentAction;
import madkit.gui.actions.MadkitActions;
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
	 * {@link MadkitActions#MADKIT_LAUNCH_NETWORK}, 
	 * {@link MadkitActions#MADKIT_STOP_NETWORK}, 
	 * {@link MadkitActions#MADKIT_EXIT_ACTION}, 
	 * {@link MadkitActions#MADKIT_ICONIFY_ALL}, 
	 * {@link MadkitActions#MADKIT_DEICONIFY_ALL}, 
	 * {@link MadkitActions#MADKIT_RESTART}, 
	 * {@link AgentAction#LOAD_LOCAL_DEMOS}, 
	 * 
	 * @param agent the agent for which this menu is created
	 */
	public MadkitToolBar(final AbstractAgent agent) {
		super("MadKit");
		add(MadkitActions.MADKIT_LAUNCH_NETWORK.getAction(agent));
		add(MadkitActions.MADKIT_STOP_NETWORK.getAction(agent));
		add(MadkitActions.MADKIT_EXIT_ACTION.getAction(agent));
		addSeparator();
		add(MadkitActions.MADKIT_ICONIFY_ALL.getAction(agent));
		add(MadkitActions.MADKIT_DEICONIFY_ALL.getAction(agent));
		add(MadkitActions.MADKIT_RESTART.getAction(agent));
//		add(MadkitActions.MADKIT_CLONE.getAction(agent));
//		add(MadkitActions.CONNECT_WEB_REPO.getAction(agent));
		add(MadkitActions.LOAD_LOCAL_DEMOS.getAction(agent));
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
