/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MadKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.INVALID_AGENT_ADDRESS;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.15
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class SendReplyWithRole extends JunitMadkit {

	@Test
	public void returnNotInGroup() {
		launchTest(new Replier() {

			protected void activate() {
				super.activate();
				assertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				assertEquals(NOT_IN_GROUP, sendReplyWithRole(nextMessage(), new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnInvalidAA() {
		launchTest(new Replier() {

			protected void activate() {
				super.activate();
				target.leaveGroup(COMMUNITY, GROUP);
				assertEquals(INVALID_AGENT_ADDRESS, sendReplyWithRole(nextMessage(), new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTest(new Replier() {

			protected void activate() {
				super.activate();
				assertEquals(SUCCESS, sendReplyWithRole(nextMessage(), new Message(), ROLE));
			}
		});
	}

	@Test
	public void returnRoleNotHandled() {
		launchTest(new Replier() {

			protected void activate() {
				super.activate();
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE2));
				assertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				assertEquals(ROLE_NOT_HANDLED, sendReplyWithRole(nextMessage(), new Message(), ROLE));
			}
		});
	}

	@Test
	public void nullArg() {
		launchTest(new Replier() {

			protected void activate() {
				try {
					assertEquals(SUCCESS, sendReplyWithRole(nextMessage(), null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);

		launchTest(new Replier() {

			protected void activate() {
				try {
					assertEquals(SUCCESS, sendReplyWithRole(null, new Message(), ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);

	}

}
