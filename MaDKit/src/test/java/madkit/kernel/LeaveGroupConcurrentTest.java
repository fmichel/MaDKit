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

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.test.agents.CGRAgent;

/**
 *
 * @version 6.0.2
 * 
 */

public class LeaveGroupConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenNullArgs_whenLeaveGroup_thenThrowsNullPointerException() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					leaveGroup(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					leaveGroup(COMMUNITY, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					leaveGroup(null, GROUP);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

	@Test
	public void givenNotCommunity_whenLeaveGroup_thenReturnsNotCommunity() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(NOT_COMMUNITY, leaveGroup(cgrDontExist(), cgrDontExist()));
				resume();
			}
		});

	}

	@Test
	public void givenNotInGroup_whenLeaveGroup_thenReturnsNotInGroup() {
		runTest(new GenericTestAgent() {
			@Override
			protected void onActivation() {
				launchAgent(new CGRAgent());
				threadAssertTrue(getOrganization().isCommunity(COMMUNITY));
				threadAssertTrue(getOrganization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_IN_GROUP, leaveGroup(COMMUNITY, GROUP));
				resume();
			}
		});
	}

	@Test
	public void givenNotGroupNotCommunity_whenLeaveGroup_thenReturnsNotGroupOrNotCommunity() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_GROUP, leaveGroup(COMMUNITY, cgrDontExist()));
				threadAssertEquals(NOT_COMMUNITY, leaveGroup(cgrDontExist(), GROUP));
				resume();
			}
		});
	}

	@Test
	public void givenGroupExists_whenLeaveGroup_thenGroupIsLeft() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertFalse(getOrganization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_COMMUNITY, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(getOrganization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertFalse(getOrganization().isCommunity(COMMUNITY));
				threadAssertFalse(getOrganization().isGroup(COMMUNITY, GROUP));

				// second run
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertTrue(getOrganization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertFalse(getOrganization().isCommunity(COMMUNITY));
				threadAssertFalse(getOrganization().isGroup(COMMUNITY, GROUP));
				resume();
			}
		});
	}

	@Test
	public void givenAgentKilledBeforeLeaveGroup_whenLeaveGroup_thenGroupIsLeft() {

		runTest(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertFalse(getOrganization().isCommunity(COMMUNITY));
				CGRAgent a = new CGRAgent();
				launchAgent(a);
				threadAssertEquals(ALREADY_GROUP, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, a.leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_IN_GROUP, a.leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, a.createGroup(COMMUNITY, GROUP));
				killAgent(a);
				threadAssertFalse(getOrganization().isCommunity(COMMUNITY));
				resume();
			}
		});
	}

}
