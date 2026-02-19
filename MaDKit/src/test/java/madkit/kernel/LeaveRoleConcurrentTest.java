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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);
				assertThat(leaveRole(COMMUNITY, GROUP, ROLE)).as("leaveRole return code").isEqualTo(SUCCESS);
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP)).as("group should still exist after leaving a role")
						.isTrue();
				assertThat(leaveRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER)).as("leaveRole for GROUP_MANAGER")
						.isEqualTo(SUCCESS);
				// leaveGroup by leaving roles
				assertThat(getOrganization().isCommunity(COMMUNITY))
						.as("community should be removed after leaving all roles").isFalse();
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP))
						.as("group should be removed after leaving all roles").isFalse();
				resume();
			}
		});
	}

	@Test
	public void givenInvalidCommunityGroupRole_whenLeaveRole_thenReturnsNotCgr() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(leaveRole(cgrDontExist(), GROUP, ROLE)).as("leaveRole with non-existent community")
						.isEqualTo(NOT_COMMUNITY);
				assertThat(leaveRole(COMMUNITY, cgrDontExist(), ROLE)).as("leaveRole with non-existent group")
						.isEqualTo(NOT_GROUP);
				assertThat(leaveRole(COMMUNITY, GROUP, cgrDontExist())).as("leaveRole with non-existent role")
						.isEqualTo(NOT_ROLE);
				assertThat(launchAgent(new DefaultTestAgent() {
					@Override
					public void behaviorInActivate() {
						requestRole(COMMUNITY, GROUP, ROLE);
						resume();
					}
				})).as("launchAgent return code when launching helper agent").isEqualTo(SUCCESS);
				assertThat(leaveRole(COMMUNITY, GROUP, ROLE)).as("leaveRole when role not handled by current agent")
						.isEqualTo(ROLE_NOT_HANDLED);
				assertThat(leaveGroup(COMMUNITY, GROUP)).as("leaveGroup return code").isEqualTo(SUCCESS);
				assertThat(leaveRole(COMMUNITY, GROUP, ROLE)).as("leaveRole when not in group").isEqualTo(NOT_IN_GROUP);
				resume();
			}
		});
	}

	@Test
	public void givenNullArgs_whenLeaveRole_thenThrowsNullPointerException() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThatThrownBy(() -> leaveRole(null, null, null)).as("leaveRole(null, null, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> leaveRole(COMMUNITY, null, null)).as("leaveRole(COMMUNITY, null, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> leaveRole(COMMUNITY, GROUP, null)).as("leaveRole(COMMUNITY, GROUP, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> leaveRole(null, GROUP, null)).as("leaveRole(null, GROUP, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> leaveRole(null, GROUP, ROLE)).as("leaveRole(null, GROUP, ROLE)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> leaveRole(null, null, ROLE)).as("leaveRole(null, null, ROLE)")
						.isInstanceOf(NullPointerException.class);
				resume();
			}
		});
	}

}
