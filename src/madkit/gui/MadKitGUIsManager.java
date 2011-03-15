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

import madkit.kernel.AbstractAgent;

/**
 * This interface could be used to define an agent responsible for gui management in MadKit.
 * 
 * @author Fabien Michel
 * @since MadKit 5
 * @version 0.9
 * 
 */
public interface MadKitGUIsManager {

	public void setupGUIOf(final AbstractAgent a);
	
	public void disposeGUIOf(final AbstractAgent abstractAgent);

	/**
	 * @param agent
	 * @return the component corresponding to this agent
	 */
	public Component getGUIComponentOf(final AbstractAgent agent);

	public void setGUILocationOf(final AbstractAgent abstractAgent, final Point location);

}
