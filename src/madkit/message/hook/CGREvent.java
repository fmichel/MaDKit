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
 * Root class of messages which are sent to agents that have requested hooks to the kernel
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
public abstract class CGREvent extends HookMessage {
	

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 6722477889792374461L;

	CGREvent(AgentActionEvent event) {
		super(event);
	}

	/**
	 * Returns the address of the agent that did
	 * the request.
	 * 
	 * @return the name of the agent that triggers the event
	 */
	public abstract AgentAddress getSourceAgent();
	
	@Override
	public String toString() {
		return super.toString()+" from "+getSourceAgent();
	}

}