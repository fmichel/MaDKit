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
import java.awt.Point;

import javax.swing.JFrame;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;

/**
 * This interface could be used to define an agent which will be responsible for gui management in MadKit.
 * Especially, if such an agent handles the role {@value Madkit.Roles#GUI_MANAGER_ROLE} 
 * in the {@value Madkit.Roles#SYSTEM_GROUP} of the {@value Madkit.Roles#LOCAL_COMMUNITY} community, then these methods will be called
 * automatically when an agent is launched and terminated.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.8
 * @version 0.9
 * 
 */
public interface GUIsManagerAgent {
	
	/**
	 * Automatically called when an agent is launched with true for GUI.
	 * @param agent the concerned agent.
	 */
	public void setupGUIOf(final AbstractAgent agent);
	
	/**
	 * Automatically called when an agent is launched with true for GUI
	 * @param agent the concerned agent.
	 */
	public void disposeGUIOf(final AbstractAgent agent);
	
//	/**
//	 * Returns the JFrame which has been created for this agent.
//	 * 
//	 * @param agent
//	 * @return the JFrame which has been created for this agent 
//	 * or <code>null</code> if it does not exist.
//	 */
//	public JFrame getFrameOf(final AbstractAgent agent);
}
