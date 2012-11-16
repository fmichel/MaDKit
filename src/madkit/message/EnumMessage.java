/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.message;

import java.util.Arrays;

/**
 * This parameterizable class could be used 
 * to build a message tagged with an enumeration 
 * and conveying any java objects using an array of Object.
 * 
 * @author Fabien Michel
 * @version 5.0
 * @since MaDKit 5.0.0.14
 *
 */
public class EnumMessage<E extends Enum<E>> extends ObjectMessage<Object[]> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 2129358510239154730L;
	private final E code;

	/**
	 * Builds a message with the specified content
	 * @param code an enum constant of type E
	 * @param parameters a list of objects 
	 */
	public EnumMessage(E code, final Object... parameters) {
		super(parameters);
		this.code = code;
	}

	@Override
	public String toString() {
		String s = super.toString()+"\n"+(getClass().getSimpleName()+getConversationID()).replaceAll(".", " ");
		return s+"    command: "+code.name()+" {"+Arrays.deepToString(getContent())+"}";
	}

	/**
	 * @return the enum constant which has been used 
	 * to construct this message
	 */
	public E getCode() {
		return code;
	}
}