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

import madkit.action.AgentAction;

/**
 * This message could be used to request a kernel's hook
 * on agent actions so that the sender will be 
 * kept informed when an agent performed this action.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.9
 * @see AgentAction
 * 
 */
public class AgentHookMessage extends EnumMessage<AgentAction> {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6079684550233257149L;

	/**
	 * This message should be used to request or release a hook on
	 * an agent action. The message should be sent to the kernel 
	 * (which is the manager of the SYSTEM group in the local
	 * community), here is
	 * an example :
	 * <pre><code>
	 * sendMessage(
	 * 	LocalCommunity.NAME,
	 * 	LocalCommunity.Groups.SYSTEM, 
	 * 	Organization.GROUP_MANAGER_ROLE,
	 * 	new AgentHookMessage(AgentAction.REQUEST_ROLE));
	 * </code></pre>
	 * In this example, the sender will be informed by the 
	 * kernel of all successful requestRole operation 
	 * made by the agents. This information is also transmitted 
	 * using this message class. {@link #getContent()} should be used
	 * to access this information. This information is coded as
	 * an array of Object. The first object is the performer's name and
	 * the next objects are the parameters which have been used to perform
	 * the action.
	 * <p>
	 * To give up the hook, just send to the kernel another message built with
	 * the same action and it will remove the sender from the subscriber list.
	 * 
	 * @param agentAction the action to monitor
	 * @param infos used by the kernel to transmit action information as 
	 * an array of Object
	 * @see AgentAction
	 */
	public AgentHookMessage(AgentAction agentAction, Object... infos) {
		super(agentAction, infos);
	}
	
	
}
