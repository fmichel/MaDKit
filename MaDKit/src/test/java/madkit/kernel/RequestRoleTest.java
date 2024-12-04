
package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import madkit.agr.DefaultMaDKitRoles;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class RequestRoleTest extends JunitMadkit {

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

	final Agent helper = new Agent() {
		protected void onActivation() {
			threadAssertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
			threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}
	};

	final Agent helper2 = new Agent() {
		protected void onActivation() {
			threadAssertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
			threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
		}
	};

	@Test
	public void returnSuccess() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
//		assertTrue(isRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
//		assertTrue(isRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void returnNotCgr() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_COMMUNITY, requestRole(cgrDontExist(), GROUP, ROLE));
				threadAssertEquals(NOT_GROUP, requestRole(COMMUNITY, cgrDontExist(), ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void returnAlreadyHandled() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(ROLE_ALREADY_HANDLED,
						requestRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void returnAccessDenied() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, alwaysDeny));
				threadAssertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, ROLE, null));
			}
		}, SUCCESS);
	}

	@Test
	public void returnAccessGranted() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, alwaysAccept));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, cgrDontExist(), null));
			}
		}, SUCCESS);
	}

	@Test
	public void defaultRole() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(getOrganization().isGroup(COMMUNITY, GROUP));
				assertTrue(getOrganization().isRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
//		threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
//		assertFalse(isGroup(COMMUNITY, GROUP));
			}
		}, SUCCESS);
	}

	@Test
	public void nullArgs() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				try {
					threadAssertEquals(NOT_COMMUNITY, requestRole(null, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, requestRole(null, null, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}

				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					threadAssertEquals(NOT_GROUP, requestRole(COMMUNITY, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, requestRole(null, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, requestRole(null, GROUP, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, requestRole(null, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, null, new Object()));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		}, SUCCESS);
	}

//
	@Test
	public void onlyOneManager() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(ROLE_ALREADY_HANDLED,
						requestRole(COMMUNITY, GROUP, DefaultMaDKitRoles.GROUP_MANAGER_ROLE));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, launchAgent(helper));
				threadAssertEquals(SUCCESS, launchAgent(helper2));
			}
		}, SUCCESS);
	}
//
//    @Test
//    public void leaveGroupByLeavingRoles() {
//	launchTestedAgent(new Agent() {
//	    protected void activate() {
//		assertFalse(isCommunity(COMMUNITY));
//		assertFalse(isGroup(COMMUNITY, GROUP));
//		threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
//		threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
//	    }
//	},SUCCESS);
//    }

}
