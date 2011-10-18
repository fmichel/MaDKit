/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.testing.util.agent;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
public class NeverStopAgent extends Agent
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	@SuppressWarnings("unused")
	private AgentAddress currentPartner = null;


	@Override
	public void activate()
	{
		createGroupIfAbsent("ping-pong","room",true, null);
		requestRole("ping-pong","room","player",null);
	}

	@Override
	public void live()
	{
		while(true){
			playing();
		}
	}

	private void playing() {
		while (true) {
			currentPartner = getAgentWithRole("ping-pong","room","player");
		}
	}
	
	@Override
	protected void end() {
		if(logger != null)
			logger.info("bye");
	}
	
	public static void main(String[] args) {
		String[] argss = {"--agentLogLevel","INFO","--launchAgents",NeverStopAgent.class.getName(),",true"};
		Madkit.main(argss);		
	}

}