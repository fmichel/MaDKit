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
package madkit.message;

import madkit.action.AgentAction;

/**
 * This message could be used to request a kernel's hook
 * on agent actions so that the sender will be 
 * kept informed when an agent performed this action.
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.14
 * @version 0.9
 * @see AgentAction
 * 
 */
public class AgentHookMessage extends EnumMessage<AgentAction> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6079684550233257149L;

	public AgentHookMessage(AgentAction code, Object... commandOptions) {
		super(code, commandOptions);
	}
	
	
}
