/*
 * Copyright 2012 Fabien Michel
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

import madkit.kernel.AgentAddress;

/**
 * A message which is sent to agents that have requested 
 * a hook on {@link HookMessage.AgentActionEvent#CREATE_GROUP}, 
 * {@link HookMessage.AgentActionEvent#REQUEST_ROLE},
 * {@link HookMessage.AgentActionEvent#LEAVE_GROUP},
 * or {@link HookMessage.AgentActionEvent#LEAVE_ROLE}
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
public class OrganizationEvent extends CGREvent{

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -7030965928934873405L;
	final private AgentAddress source;

	public OrganizationEvent(AgentActionEvent agentAction, AgentAddress source) {
		super(agentAction);
		this.source = source;
	}

	public AgentAddress getSourceAgent() {
		return source;
	}
}
