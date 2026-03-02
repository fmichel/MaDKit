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

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.testing.agents.CGRAgent;
import madkit.testing.agents.ThreadedTestAgent;

/**
 *
 * @version 6.0.5
 * 
 */

public class LeaveGroupTest extends MadkitConcurrentTestCase {

	@Test
	public void givenNullArgs_whenLeaveGroup_thenThrowsNullPointerException() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThatThrownBy(() -> leaveGroup(null, null)).as("leaveGroup(null, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> leaveGroup(COMMUNITY, null)).as("leaveGroup(COMMUNITY, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> leaveGroup(null, GROUP)).as("leaveGroup(null, GROUP)")
						.isInstanceOf(NullPointerException.class);
				resume();
			}
		});
	}

	@Test
	public void givenNotCommunity_whenLeaveGroup_thenReturnsNotCommunity() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(leaveGroup(cgrDontExist(), cgrDontExist()))
						.as("leaveGroup on non-existent community should return NOT_COMMUNITY").isEqualTo(NOT_COMMUNITY);
				resume();
			}
		});

	}

	@Test
	public void givenNotInGroup_whenLeaveGroup_thenReturnsNotInGroup() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				launchAgent(new CGRAgent());
				assertThat(getOrganization().isCommunity(COMMUNITY)).as("community exists").isTrue();
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP)).as("group exists").isTrue();
				assertThat(leaveGroup(COMMUNITY, GROUP)).as("leaveGroup when not in group").isEqualTo(NOT_IN_GROUP);
				resume();
			}
		});
	}

	@Test
	public void givenNotGroupNotCommunity_whenLeaveGroup_thenReturnsNotGroupOrNotCommunity() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(leaveGroup(COMMUNITY, cgrDontExist()))
						.as("leaveGroup with non-existent group should return NOT_GROUP").isEqualTo(NOT_GROUP);
				assertThat(leaveGroup(cgrDontExist(), GROUP))
						.as("leaveGroup with non-existent community should return NOT_COMMUNITY").isEqualTo(NOT_COMMUNITY);
				resume();
			}
		});
	}

	@Test
	public void givenGroupExists_whenLeaveGroup_thenGroupIsLeft() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP)).as("group initially present").isFalse();
				assertThat(leaveGroup(COMMUNITY, GROUP)).as("leaveGroup on missing community").isEqualTo(NOT_COMMUNITY);
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP)).as("group exists after creation").isTrue();
				assertThat(leaveGroup(COMMUNITY, GROUP)).as("leaveGroup should return SUCCESS").isEqualTo(SUCCESS);
				assertThat(getOrganization().isCommunity(COMMUNITY)).as("community should be removed after leaving")
						.isFalse();
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP)).as("group should be removed after leaving")
						.isFalse();

				// second run
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code (second run)").isEqualTo(SUCCESS);
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP)).as("group exists after creation (second run)")
						.isTrue();
				assertThat(leaveGroup(COMMUNITY, GROUP)).as("leaveGroup should return SUCCESS (second run)")
						.isEqualTo(SUCCESS);
				assertThat(getOrganization().isCommunity(COMMUNITY))
						.as("community should be removed after leaving (second run)").isFalse();
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP))
						.as("group should be removed after leaving (second run)").isFalse();
				resume();
			}
		});
	}

	@Test
	public void givenAgentKilledBeforeLeaveGroup_whenLeaveGroup_thenGroupIsLeft() {

		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(getOrganization().isCommunity(COMMUNITY)).as("community initially present").isFalse();
				CGRAgent a = new CGRAgent();
				launchAgent(a);
				assertThat(createGroup(COMMUNITY, GROUP))
						.as("createGroup with existing agent should return ALREADY_GROUP or SUCCESS")
						.isIn(ALREADY_GROUP, SUCCESS);
				assertThat(a.leaveGroup(COMMUNITY, GROUP)).as("agent leaving group returns").isEqualTo(SUCCESS);
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code after agent left").isEqualTo(SUCCESS);
				assertThat(a.leaveGroup(COMMUNITY, GROUP)).as("agent leaving group when not in it").isEqualTo(NOT_IN_GROUP);
				assertThat(leaveGroup(COMMUNITY, GROUP)).as("leaveGroup should return SUCCESS").isEqualTo(SUCCESS);
				assertThat(a.createGroup(COMMUNITY, GROUP)).as("other agent createGroup should return SUCCESS")
						.isEqualTo(SUCCESS);
				killAgent(a);
				assertThat(getOrganization().isCommunity(COMMUNITY)).as("community should be removed after killing agent")
						.isFalse();
				resume();
			}
		});
	}

}
