/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/

package madkit.kernel;

import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.agr.SystemRoles;

/**
 *
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class RequestRoleConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgent_whenRequestRole_thenReturnSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenRequestRoleWithNonExistentCgr_thenReturnNotCgr() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_COMMUNITY, requestRole(cgrDontExist(), GROUP, ROLE));
				threadAssertEquals(NOT_GROUP, requestRole(COMMUNITY, cgrDontExist(), ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenBuggyGate_thenThrowException() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, (_, _, _) -> {
					throw new NullPointerException();
				}));
				try {
					requestRole(COMMUNITY, GROUP, ROLE);
					threadFail("Exception not thrown");
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				resume();
			}
		});

	}

	@Test
	public void givenAgent_whenRequestRoleAlreadyHandled_thenReturnAlreadyHandled() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenRequestRoleWithAccessDenied_thenReturnAccessDenied() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, (_, _, _) -> false));
				threadAssertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, ROLE, null));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenRequestRoleWithAccessGranted_thenReturnAccessGranted() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, (_, _, _) -> true));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, cgrDontExist(), null));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenDefaultRole_thenReturnSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(getOrganization().isGroup(COMMUNITY, GROUP));
				assertTrue(getOrganization().isRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenRequestRoleWithNullArgs_thenHandleNullPointerException() {
		runTest(new Agent() {
			@Override
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
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenOnlyOneManager_thenReturnRoleAlreadyHandled() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				final Agent helper = new Agent() {
					@Override
					protected void onActivation() {
						threadAssertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER));
						threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
					}
				};

				final Agent helper2 = new Agent() {
					@Override
					protected void onActivation() {
						threadAssertEquals(ACCESS_DENIED, requestRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER));
						threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
					}
				};
				threadAssertEquals(SUCCESS, launchAgent(helper));
				threadAssertEquals(SUCCESS, launchAgent(helper2));
				resume();
			}
		});
	}
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
