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

import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.testing.util.agent.NormalAA;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.15
 * @version 0.91
 * 
 */
@SuppressWarnings("serial")
public class SendReplyTest extends JunitMadkit {

	@Test
	public void returnNotInGroup() {
		launchTest(new Replier() {

			protected void activate() {
				super.activate();
				assertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				assertEquals(NOT_IN_GROUP, sendReply(nextMessage(), new Message()));
			}
		});
	}
	
	@Test
	public void returnInvalidAA() {
		launchTest(new Replier() {

			protected void activate() {
				super.activate();
				target.leaveGroup(COMMUNITY, GROUP);
				assertEquals(INVALID_AGENT_ADDRESS, sendReply(nextMessage(), new Message()));
			}
		});
	}
	
	
	@Test
	public void returnSuccess() {
		launchTest(new Replier() {

			protected void activate() {
				super.activate();
				assertEquals(SUCCESS, sendReply(nextMessage(), new Message()));
			}
		});
	}
	
	
	@Test
	public void nullArg() {
		launchTest(new Replier() {

			protected void activate() {
				try {
					assertEquals(SUCCESS, sendReply(nextMessage(), null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		},ReturnCode.AGENT_CRASH);

		launchTest(new Replier() {

			protected void activate() {
				try {
					assertEquals(SUCCESS, sendReply(null, new Message()));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		},ReturnCode.AGENT_CRASH);

		launchTest(new Replier() {

			protected void activate() {
				try {
					assertEquals(SUCCESS, sendReply(null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		},ReturnCode.AGENT_CRASH);
}
	
	
}

@SuppressWarnings("serial")
class Replier extends AbstractAgent {

	protected AbstractAgent	target;

	protected void activate() {
		assertEquals(SUCCESS, launchAgent(target = new NormalAA()));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		assertEquals(SUCCESS, target.sendMessage(COMMUNITY, GROUP, ROLE, new Message()));
	}

}