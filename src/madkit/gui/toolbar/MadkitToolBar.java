/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import javax.swing.JToolBar;

import madkit.action.GUIManagerAction;
import madkit.action.GlobalAction;
import madkit.action.KernelAction;
import madkit.gui.SwingUtil;
import madkit.kernel.AbstractAgent;

/**
 * An out of the box toolbar for MaDKit based applications.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.92
 * 
 */
public class MadkitToolBar extends JToolBar {

	/**
	 * 
	 */
	private static final long serialVersionUID = -700298646422969523L;

	/**
	 * Creates a {@link JToolBar} featuring: 
	 * <ul>
	 * <li> {@link KernelAction#EXIT}
	 * <li> {@link KernelAction#COPY}
	 * <li> {@link KernelAction#RESTART}
	 * <li> {@link KernelAction#LAUNCH_NETWORK}
	 * <li> {@link KernelAction#STOP_NETWORK}
	 * <li> {@link GUIManagerAction#CONNECT_TO_IP}
	 * <li> {@link GlobalAction#JCONSOLE}
	 * <li> {@link KernelAction#CONSOLE}
	 * <li> {@link GlobalAction#DEBUG}
	 * <li> {@link GlobalAction#LOAD_LOCAL_DEMOS}
	 * <li> {@link GlobalAction#LOAD_JAR_FILE}
	 * <li> {@link GUIManagerAction#ICONIFY_ALL}
	 * <li> {@link GUIManagerAction#DEICONIFY_ALL}
	 * <li> {@link GUIManagerAction#KILL_AGENTS}
	 * </ul>
	 * 
	 * @param agent the agent for which this menu is created
	 */
	public MadkitToolBar(final AbstractAgent agent) {
		super("MaDKit");
		SwingUtil.addMaDKitActionsTo(this, agent);
		SwingUtil.scaleAllAbstractButtonIconsOf(this, 20);
	}
}
