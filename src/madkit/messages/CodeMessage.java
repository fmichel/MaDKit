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
package madkit.messages;

import madkit.kernel.Message;

/**
 * This parameterizable class could be used to code a message
 * with an enumeration and to convey 
 * any Java Object with it.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 *
 */
public class CodeMessage<E extends Enum<E>,T> extends ObjectMessage<T> {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8934501592403513198L;
	private final E code;

	/**
	 * Builds a message with the specified content
	 * @param content
	 */
	public CodeMessage(E code, final T content) {
		super(content);
		this.code = code;
	}

	@Override
	public String toString() {
		return super.toString()+" "+code;
	}

	public E getCode() {
		return code;
	}
}
