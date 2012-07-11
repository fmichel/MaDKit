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

import java.util.Arrays;

/**
 * Thrown to indicate that the agent is trying to use
 * a method while not launched or already dead.
 *  
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 1.0
 * 
 */
final public class KernelException extends RuntimeException {


	private static final long serialVersionUID = 8942343591444752340L;

	KernelException(final String message){
		super(message);//TODO clean stack trace
		setStackTrace(Arrays.asList(getStackTrace()).subList(3, getStackTrace().length-1).toArray(new StackTraceElement[0]));
	}
}