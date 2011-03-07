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
package madkit.kernel.gui;

import java.awt.Point;
import java.io.OutputStream;

/**
 * This interface defines methods which
 * should be implemented by GUI components that will be 
 * directly managed by MadKit.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.3
 * @version 0.9
 *
 */
public interface AgentGUIModel {

	/**
	 * returns the output stream to which log messages, or other
	 * info could be forwarded to.
	 * @return the output stream for this component.
	 */
	public OutputStream getOutputStream();
	
	/**
	 * Sets the location which will be used 
	 * to position the component on the screen.
	 * @param GUIlocation the location on the screen.
	 */
	public void setGUIPreferredlocation(Point GUIlocation);
	
	/**
	 * Gets the preferred location for this GUI.
	 * @return the preferred location on the screen
	 */
	public Point getGUIPreferredlocation();
	
	/**
	 * Prints a message in the GUI.
	 * @param aMessage the message to display in the GUI.
	 */
	public void print(String aMessage);

}
