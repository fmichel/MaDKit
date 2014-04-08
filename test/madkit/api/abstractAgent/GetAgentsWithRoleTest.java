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

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class GetAgentsWithRoleTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		protected void activate() {
			assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
		}
	};

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					getAgentsWithRole(null, null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
					assertNull(getAgentsWithRole(COMMUNITY, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(COMMUNITY, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(null, GROUP, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(null, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
				try {
					assertNull(getAgentsWithRole(COMMUNITY, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					// e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void returnNull() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(COMMUNITY, GROUP, ROLE));
				assertNull(getAgentsWithRole(aa(), GROUP, ROLE));
				assertNull(getAgentsWithRole(COMMUNITY, aa(), ROLE));
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, aa()));
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, target.leaveRole(COMMUNITY, GROUP, ROLE));
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
				launchAgent(target);
				assertEquals(SUCCESS, target.requestRole(COMMUNITY, GROUP, ROLE));
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(1, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
				launchAgent(new AbstractAgent() {
					protected void activate() {
						assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
					}
				});
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(2, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
			}
		});
	}

	@Test
	public void returnNotNullOnManagerRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				launchAgent(target);
				assertNotNull(getAgentsWithRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(1, getAgentsWithRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE).size());
			}
		});
	}

	@Test
	public void returnNullOnManagerRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertNull(getAgentsWithRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
			}
		});
	}
}
