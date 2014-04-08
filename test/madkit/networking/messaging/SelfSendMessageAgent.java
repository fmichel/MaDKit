/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.networking.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */

public class SelfSendMessageAgent extends Agent {

	/*
	 * (non-Javadoc)
	 * 
	 * @see test.util.OrgTestAgent#activate()
	 */
	
	
	public SelfSendMessageAgent() {
		createGUIOnStartUp();
	}
	
	@Override
	protected void live() {
		setLogLevel(Level.ALL);
		assertEquals(SUCCESS, createGroup(JunitMadkit.COMMUNITY, JunitMadkit.GROUP, true));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		AgentAddress aa = getAgentAddressIn(JunitMadkit.COMMUNITY, JunitMadkit.GROUP, JunitMadkit.ROLE);
		sendMessage(aa, new Message());
		if(logger != null)
			logger.info(nextMessage().toString());
	}
	
	public static void main(String[] args) {
		executeThisAgent();
	}
}