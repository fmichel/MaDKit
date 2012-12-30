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

import madkit.kernel.AbstractAgent;


/**
 * A message which is sent to agents that have requested 
 * a hook on {@link HookMessage.AgentActionEvent#AGENT_STARTED} or {@link HookMessage.AgentActionEvent#AGENT_TERMINATED}
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.19
 * @version 0.9
 * 
 */
public class AgentLifeEvent extends HookMessage {

	/**
	 * 
	 */
	private static final long	serialVersionUID	= -5737575514238878308L;
	final private AbstractAgent	 source;

	public AgentLifeEvent(AgentActionEvent agentAction, AbstractAgent agent) {
		super(agentAction);
		source = agent;
	}

	/**
	 * The agent which is related to this event
	 * 
	 * @return the source agent of the event
	 */
	public AbstractAgent getSource() {
		return source;
	}
	
	/**
	 * Return the ID of the agent, i.e. its hashCode value
	 * 
	 * @return the ID of the agent
	 */
	String getSourceAgentID(){
		return ""+source.hashCode();
	}
	
	@Override
	public String toString() {
		return super.toString()+" from "+source;
	}
	
}
