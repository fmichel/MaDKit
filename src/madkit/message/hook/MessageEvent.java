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
import madkit.kernel.Message;


/**
 * A message which is sent to agents that have requested 
 * a hook on {@link HookMessage.AgentActionEvent#AGENT_STARTED} or {@link HookMessage.AgentActionEvent#AGENT_TERMINATED}
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.21
 * @version 0.9
 * 
 */
public class MessageEvent extends CGREvent {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= 7792908692169144580L;
	final private Message	message;

	public MessageEvent(final AgentActionEvent agentAction, final Message m) {
		super(agentAction);
		message = m;
	}
	
	/**
	 * @return the exchanged message
	 */
	public Message getMessage(){
		return message;
	}

	@Override
	public AgentAddress getSourceAgent() {
		return message.getSender();
	}
	
	@Override
	public String toString() {
		return super.toString()+"\n\tdetails : ------> "+message;
	}

}
