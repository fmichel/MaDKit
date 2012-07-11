/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.kernel;

import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;

/**
 * Every regular MaDKit Options implements this interface.
 * 
 * @see Option
 * @see LevelOption
 * @see BooleanOption
 * @see AbstractAgent#getMadkitProperty(String)
 * @see AbstractAgent#setMadkitProperty(String, String)
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
public interface MadkitOption {

	/**
	 * Returns the string form of the option as it should be used
	 * in a command line or with the {@link Madkit#main(String[])} method.
	 * @return The command line form for this option, with -- in front of 
	 * the option's name, i.e. <code><b>--optionName</b></code> 
	 */
	public String toString();
	
	/**
	 * Returns the option's name. This is a call to
	 * {@link Enum#name()}
	 * @return the option's name
	 */
	public String name();
}
