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
package madkit.networking.messaging;

import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Message;
import madkit.kernel.Madkit.LevelOption;
import test.util.JUnitBooterAgent;
import test.util.OrgTestAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
public class NetworkMessagingTest extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -2226317432181332931L;


	@Override
	public void activate() {
		launchAgent(new NetworkMessageAgentTest());
	}
	
	
	/* (non-Javadoc)
	 * @see test.utils.JUnitBooterAgent#madkitInit()
	 */
	@Override
	public void madkitInit() {
		String[] args = {"--network","--agentLogLevel","ALL","--"+LevelOption.madkitLogLevel,"ALL","--orgLogLevel","ALL","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}
}


class NetworkMessageAgentTest extends OrgTestAgent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8456452767330561605L;

	/* (non-Javadoc)
	 * @see test.util.OrgTestAgent#activate()
	 */
	@Override
	protected void live() {
		AgentAddress aa = getAgentWithRole("test", "test", "test");
		if(aa == null)
			waitNextMessage();
		else
			sendMessage(aa, new Message());
	}
}