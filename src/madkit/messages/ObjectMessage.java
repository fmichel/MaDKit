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
 * This parameterizable class could be used to convey 
 * any Java Object between MadKit agents.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.1
 * @version 0.9
 *
 */
public class ObjectMessage<T> extends Message {

	private static final long serialVersionUID = 2061462024105569662L;
	private final T content;
	
	/**
	 * Builds a message with the specified content
	 * @param content
	 */
	public ObjectMessage(final T content) {
		this.content = content;
	}

	/**
	 * Gets the content of this message
	 * @return the object of type T contained in the message
	 */
	public T getContent() {
		return content;
	}
	
	/**
	 * @see madkit.kernel.Message#toString()
	 */
	@Override
	public String toString() {
		return super.toString()+"\n\tcontent : {"+content+"}";
	}
}
