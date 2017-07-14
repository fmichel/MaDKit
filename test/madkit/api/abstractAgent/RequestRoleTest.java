/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
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

import madkit.agr.DefaultMaDKitRoles;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Gatekeeper;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class RequestRoleTest extends JunitMadkit {

	final AbstractAgent helper = new AbstractAgent() {
		protected void activate() {
			assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}
	};

	final AbstractAgent helper2 = new AbstractAgent() {
		protected void activate() {
			assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
			assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}
	};

	final Gatekeeper alwaysDeny = new Gatekeeper() {
		@Override
		public boolean allowAgentToTakeRole(String requesterID, String roleName, Object memberCard) {
			return false;
		}
	};

	final Gatekeeper alwaysAccept = new Gatekeeper() {
		@Override
		public boolean allowAgentToTakeRole(String requesterID, String roleName, Object memberCard) {
			return true;
		}
	};

	final Gatekeeper buggyIdentifier = new Gatekeeper() {
		@SuppressWarnings("null")
		@Override
		public boolean allowAgentToTakeRole(String requesterID, String roleName, Object memberCard) {
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
				assertTrue(isRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
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
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
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
				assertTrue(isRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
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
				assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
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
