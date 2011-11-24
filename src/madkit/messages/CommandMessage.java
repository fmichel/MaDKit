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

import java.util.Arrays;

/**
 * For now its purpose is to allow agents to send to the kernel agent
 * some MadKit commands such as launchAgent.
 * 
 * @author Fabien Michel
 * @version 5.0
 * @since MadKit 5.0.0.14
 *
 */
public class CommandMessage<E extends Enum<E>> extends ObjectMessage<Object[]> {
	
	private final E code;

	/**
	 * Builds a message with the specified content
	 * @param content
	 */
	public CommandMessage(E code, final Object... commandOptions) {
		super(commandOptions);
		this.code = code;
	}

	@Override
	public String toString() {
		return super.toString()+"\n\tcommand : "+code.name()+" "+Arrays.deepToString(getContent());
	}

	public E getCode() {
		return code;
	}
	
	public static <E extends Enum<E>> void proceedMessage(CommandMessage<E> cm){
		cm.getCode().name();
		System.err.println(cm.getCode().name());
	}
	
	public static void main(String[] args) {
		CommandMessage<Test> name = new CommandMessage<Test>(Test.TT,"a",2);
		if(name instanceof CommandMessage<?>){
			if(name.getCode().getClass() == Test.class){
				System.err.println("yes");
			}
		}
		proceedMessage(name);
	}
}
enum Test2 {
	TT;
}
enum Test {
	TT;
}