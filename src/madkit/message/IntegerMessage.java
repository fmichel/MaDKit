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
package madkit.message;

/**
 * A message class that conveys an integer.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.1
 * @version 0.9
 * 
 */
public class IntegerMessage extends ObjectMessage<Integer> {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 1L;

	/**
	 * Builds a new message containing the integer i
	 * @param i the integer
	 */
	public IntegerMessage(Integer i) {
		super(i);
	}

}
