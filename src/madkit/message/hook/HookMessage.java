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
package madkit.message.hook;

import madkit.message.EnumMessage;

/**
 * This message could be used to request a kernel's hook
 * on agent actions so that the sender will be 
 * kept informed when an agent performed particular action.
 * 
 * @author Fabien Michel
 * @since MaDKit 5.0.0.14
 * @version 0.91
 * @see AgentActionEvent
 * 
 */
public class HookMessage extends EnumMessage<HookMessage.AgentActionEvent> {
	

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 2751873865788097297L;

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
	 * 	new HookMessage(AgentActionEvent.REQUEST_ROLE));
	 * </code></pre>
	 * In this example, the sender will be informed by the 
	 * kernel of all successful requestRole operation 
	 * made by the agents. This information will be transmitted 
	 * using a subclass of HookMessage depending on the nature of the event.
	 * That is, {@link OrganizationEvent}, {@link MessageEvent} or
	 * {@link AgentLifeEvent} messages will be sent by the kernel
	 * according to the type of the hook which has been requested.
	 * <p>
	 * To give up the hook, just send to the kernel another message built with
	 * the same action and it will remove the sender from the subscriber list.
	 * 
	 * @param hookType the action to monitor
	 * @param infos used by the kernel to transmit action information as 
	 * an array of objects
	 * @see AgentActionEvent
	 */
	public HookMessage(HookMessage.AgentActionEvent hookType, Object... infos) {
		super(hookType, infos);
	}
	
	/**
	 * Enumeration representing agent actions that could be monitored using hooks.
	 *
	 */
	public enum AgentActionEvent{
		CREATE_GROUP,
		REQUEST_ROLE,
		LEAVE_GROUP,
		LEAVE_ROLE,
		SEND_MESSAGE,
		BROADCAST_MESSAGE,
		AGENT_STARTED,
		AGENT_TERMINATED
//		LAUNCH_AGENT,
//		RELOAD, 
//		KILL_AGENT
	}
}
