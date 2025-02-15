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

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_ROLE;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.agr.SystemRoles;

/**
 *
 * @version 6.0.2
 * 
 */

public class LeaveRoleConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenGroupAndRole_whenLeaveRole_thenReturnsSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertTrue(getOrganization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER));
				// leaveGroup by leaving roles
				threadAssertFalse(getOrganization().isCommunity(COMMUNITY));
				threadAssertFalse(getOrganization().isGroup(COMMUNITY, GROUP));
				resume();
			}
		});
	}

	@Test
	public void givenInvalidCommunityGroupRole_whenLeaveRole_thenReturnsNotCgr() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_COMMUNITY, leaveRole(cgrDontExist(), GROUP, ROLE));
				threadAssertEquals(NOT_GROUP, leaveRole(COMMUNITY, cgrDontExist(), ROLE));
				threadAssertEquals(NOT_ROLE, leaveRole(COMMUNITY, GROUP, cgrDontExist()));
				threadAssertEquals(SUCCESS, launchAgent(new Agent() {
					@Override
					protected void onActivation() {
						requestRole(COMMUNITY, GROUP, ROLE);
					}
				}));
				threadAssertEquals(ROLE_NOT_HANDLED, leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_IN_GROUP, leaveRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenNullArgs_whenLeaveRole_thenThrowsNullPointerException() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveRole(null, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_GROUP, leaveRole(COMMUNITY, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(ROLE_NOT_HANDLED, leaveRole(COMMUNITY, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveRole(null, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveRole(null, GROUP, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveRole(null, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

}
