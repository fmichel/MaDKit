/*
 * Copyright 1997-2014 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.testing.util.agent.ForEverAgent;
import madkit.testing.util.agent.ForEverReplierAgent;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.4
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class getReplyToTest extends JunitMadkit {

	@Test
	public void getReplyToSuccess() {
		launchTest(new NormalAgent() {
			protected void activate() {
				super.activate();
				assertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
				final Message message = new Message();
				sendMessage(COMMUNITY, GROUP, ROLE, message);
				sendMessage(COMMUNITY, GROUP, ROLE, new Message());
				pause(20);
				receiveMessage(new Message());
				assertNotNull(getReplyTo(message));
				assertEquals(3, nextMessages(null).size());
			}
		});
	}

	@Test
	public void getReplyToIsNull() {
		launchTest(new NormalAgent() {
			protected void activate() {
				super.activate();
				ForEverAgent a = new ForEverAgent();
				launchAgent(a);
				AgentAddress aa = a.getAgentAddressIn(COMMUNITY, GROUP, ROLE);
				final Message message = new Message();
				sendMessage(aa, message);
				killAgent(a);
				assertEquals(SUCCESS, launchAgent(new ForEverReplierAgent()));
				sendMessage(COMMUNITY, GROUP, ROLE, new Message());
				pause(20);
				receiveMessage(new Message());
				assertNull(getReplyTo(message));
				assertEquals(4, nextMessages(null).size());
			}
		});
	}

	@Test
	public void nullArg() {
		launchTest(new NormalAgent() {
			protected void activate() {
				try {
					getReplyTo(null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, AGENT_CRASH);
	}

}
