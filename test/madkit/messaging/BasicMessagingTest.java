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
package madkit.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AA;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_ROLE;
import static madkit.kernel.AbstractAgent.ReturnCode.NO_RECIPIENT_FOUND;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import test.util.JUnitBooterAgent;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
public class BasicMessagingTest extends JUnitBooterAgent{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7779039887981930633L;
	AbstractAgent other = new AbstractAgent();
	AbstractAgent other2 = new AbstractAgent();
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		ReturnCode code;
		launchAgent(other);
		launchAgent(other2);
		/////////////////////////// REQUEST ROLE ////////////////////////

		code = testAgent.createGroup("public", "system", false,null);
		assertEquals(SUCCESS,code);
		code = testAgent.requestRole("public", "system", "site");
		assertEquals(SUCCESS,code);
		code = other.requestRole("public", "system", "other");
		assertEquals(SUCCESS,code);
		code = other2.requestRole("public", "system", "other");
		assertEquals(SUCCESS,code);
		
		//////////////////////// REQUESTS ///////////////////
		AgentAddress aa = other.getAgentWithRole("public", "system", "site");
		assertNotNull(aa);

		assertEquals(SUCCESS,other.sendMessage(null, new Message()));
		assertEquals(SUCCESS,other.sendMessage(aa, null));
		
		code = other.sendMessage(aa, new Message());
		assertEquals(SUCCESS,code);

		
		Message m = testAgent.nextMessage();
		assertNotNull(m);
		if(testAgent.getLogger() != null)
			testAgent.getLogger().info("receive : "+m);

	
		code = testAgent.leaveRole("public", "system", "site");
		assertEquals(SUCCESS,code);
		
		//aa no longer exists
		code = other.sendMessage(aa, new Message());
		assertEquals(INVALID_AA,code);

		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);

		code = testAgent.sendMessage("public", "system", "site", new Message());
		assertEquals(NO_RECIPIENT_FOUND,code);

		code = testAgent.sendMessage("publicc", "system", "site", new Message());
		assertEquals(NOT_COMMUNITY,code);

		code = testAgent.sendMessage("public", "systemm", "site", new Message());
		assertEquals(NOT_GROUP,code);

		code = testAgent.sendMessage("public", "system", "sitee", new Message());
		assertEquals(NOT_ROLE,code);

		code = testAgent.sendMessage("public", "system", "site", new Message());
		assertEquals(NO_RECIPIENT_FOUND,code);

		code = testAgent.broadcastMessage("public", "system", "other", new Message());
		assertEquals(SUCCESS,code);

		// testing reception
		m = other.nextMessage();
		assertNotNull(m);
		m = other.nextMessage();
		assertNull(m);
		m = other2.nextMessage();
		assertNotNull(m);
		m = other2.nextMessage();
		assertNull(m);
		
		//testing warning
		code = testAgent.broadcastMessage("public", "system", "site", new Message());
		assertEquals(NO_RECIPIENT_FOUND,code);

		code = testAgent.broadcastMessage("publicc", "system", "other", new Message());
		assertEquals(NOT_COMMUNITY,code);

		code = testAgent.broadcastMessage("public", "systemm", "other", new Message());
		assertEquals(NOT_GROUP,code);

		code = testAgent.broadcastMessage("public", "system", "sitee", new Message());
		assertEquals(NOT_ROLE,code);

	}

}
