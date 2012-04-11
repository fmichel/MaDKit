/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadKit;
import madkit.kernel.AbstractAgent.ReturnCode;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.16
 * @version 0.9
 * 
 */
@SuppressWarnings("all")
public class CreateGroupIfAbsentTest extends JunitMadKit {

	final Gatekeeper gi = new Gatekeeper() {
		@Override
		public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
			return false;
		}
	};

	@Test
	public void returnFalse() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertFalse(createGroupIfAbsent(COMMUNITY, GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertTrue(createGroupIfAbsent(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void communityIsNull() {
		addMadkitArgs("--kernelLogLevel", "ALL");
			launchTest(new AbstractAgent() {
				protected void activate() {
					try {
						createGroupIfAbsent(null, aa());
						noExceptionFailure();
					} catch (NullPointerException e) {
						throw e;
					}
				}
			},ReturnCode.AGENT_CRASH);
	}

	@Test
	public void groupIsNull() {
		addMadkitArgs("--kernelLogLevel", "ALL");
			launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					createGroupIfAbsent(aa(), null);
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
					createGroupIfAbsent(null, null,true);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					createGroupIfAbsent(aa(), null, false, null);
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
				assertTrue(createGroupIfAbsent(COMMUNITY, GROUP));
				assertTrue(isCommunity(COMMUNITY));
				assertTrue(isGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertTrue(createGroupIfAbsent(COMMUNITY, GROUP));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY, GROUP));
			}
		});
	}

}
