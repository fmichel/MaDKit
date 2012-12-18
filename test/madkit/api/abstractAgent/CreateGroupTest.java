/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class CreateGroupTest extends JunitMadkit {

	final Gatekeeper gi = new Gatekeeper() {
		@Override
		public boolean allowAgentToTakeRole(String requesterID, String roleName, Object memberCard) {
			return false;
		}
	};

	@Test
	public void createGroup() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, createGroup(aa(), aa(), true));
				assertEquals(SUCCESS, createGroup(aa(), aa(), false));
				assertEquals(SUCCESS, createGroup(aa(), aa(), true, gi));
				assertEquals(SUCCESS, createGroup(aa(), aa(), false, gi));
			}
		});
	}

	@Test
	public void createGroupAlreadyDone() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(ALREADY_GROUP, createGroup(COMMUNITY, GROUP));
				assertTrue(isGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void communityIsNull() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					createGroup(null, aa());
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		},ReturnCode.AGENT_CRASH);
	}

	@Test
	public void nullArgs() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					createGroup(aa(), null, false, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		},ReturnCode.AGENT_CRASH);
	}

	@Test
	public void createGroupAndLeaveAndCreate() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY, GROUP));
			}
		});
	}

}
