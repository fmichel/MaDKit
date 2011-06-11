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

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.JunitMadKit.COMMUNITY;
import static madkit.kernel.JunitMadKit.GROUP;
import static madkit.kernel.JunitMadKit.ROLE;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Message;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class NetworkMessageAgent extends Agent{
	

	/* (non-Javadoc)
	 * @see test.util.OrgTestAgent#activate()
	 */
	@Override
	protected void live() {
		setLogLevel(Level.ALL);
		assertEquals(ALREADY_GROUP, createGroup(JunitMadKit.COMMUNITY,JunitMadKit.GROUP,true));
		assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
		AgentAddress aa = getAgentWithRole(JunitMadKit.COMMUNITY,JunitMadKit.GROUP,JunitMadKit.ROLE);
		sendMessage(aa, new Message());
	}
}