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

import static madkit.kernel.AbstractAgent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.agr.Organization;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.7
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class RequestRoleTest extends JunitMadKit {

	final AbstractAgent helper = new AbstractAgent() {
		protected void activate() {
			assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}
	};

	final AbstractAgent helper2 = new AbstractAgent() {
		protected void activate() {
			assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}
	};

	final Gatekeeper alwaysDeny = new Gatekeeper() {
		@Override
		public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
			return false;
		}
	};

	final Gatekeeper alwaysAccept = new Gatekeeper() {
		@Override
		public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
			return true;
		}
	};

	final Gatekeeper buggyIdentifier = new Gatekeeper() {
		@SuppressWarnings("null")
		@Override
		public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
			Object o = null;
			o.toString();
			return true;
		}
	};

	@Test
	public void returnSuccess() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertTrue(isRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertTrue(isRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void returnNotCgr() {
		addMadkitArgs(
		// "--"+Madkit.warningLogLevel,"SEVERE",
				"--" + LevelOption.agentLogLevel, "ALL");
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(NOT_COMMUNITY, requestRole(aa(), GROUP, ROLE));
				assertEquals(NOT_GROUP, requestRole(COMMUNITY, aa(), ROLE));
			}
		});
	}

	@Test
	public void returnAlreadyHandled() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void returnAccessDenied() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, alwaysDeny));
				assertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, ROLE, null));
			}
		});
	}

	@Test
	public void returnAccessGranted() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, alwaysAccept));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, aa(), null));
			}
		});
	}

	@Test
	public void buggyIdentifier() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, buggyIdentifier));// TODO
																														// think
																														// about
																														// that
																														// issue
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, aa(), null));
			}
		}, AGENT_CRASH);
	}

	@Test
	public void returnNullRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, alwaysDeny));
			}
		});
	}

	@Test
	public void defaultRole() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(isGroup(COMMUNITY, GROUP));
				assertTrue(isRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertFalse(isGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				try {
					assertEquals(NOT_COMMUNITY, requestRole(null, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, requestRole(null, null, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					assertEquals(NOT_GROUP, requestRole(COMMUNITY, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, requestRole(null, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, requestRole(null, GROUP, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(NOT_COMMUNITY, requestRole(null, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, null, new Object()));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void onlyOneManager() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, Organization.GROUP_MANAGER_ROLE));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				assertEquals(SUCCESS, launchAgent(helper));
				assertEquals(SUCCESS, launchAgent(helper2));
			}
		});
	}

	@Test
	public void leaveGroupByLeavingRoles() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

}
