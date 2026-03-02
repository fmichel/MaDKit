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

import static madkit.kernel.Agent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.agr.SystemRoles;
import madkit.testing.agents.ThreadedTestAgent;

/**
 *
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class RequestRoleTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgent_whenRequestRole_thenReturnSuccess() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole return code").isEqualTo(SUCCESS);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenRequestRoleWithNonExistentCgr_thenReturnNotCgr() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(requestRole(cgrDontExist(), GROUP, ROLE)).as("requestRole on non-existent community")
						.isEqualTo(NOT_COMMUNITY);
				assertThat(requestRole(COMMUNITY, cgrDontExist(), ROLE)).as("requestRole on non-existent group")
						.isEqualTo(NOT_GROUP);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenBuggyGate_thenThrowException() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP, false, (_, _, _) -> {
					throw new NullPointerException();
				})).as("createGroup with buggy gate").isEqualTo(SUCCESS);
				assertThatThrownBy(() -> requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole with buggy gate")
						.isInstanceOf(NullPointerException.class);
				resume();
			}
		});

	}

	@Test
	public void givenAgent_whenRequestRoleAlreadyHandled_thenReturnAlreadyHandled() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER)).as("requestRole GROUP_MANAGER")
						.isEqualTo(ROLE_ALREADY_HANDLED);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole ROLE").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole ROLE already handled")
						.isEqualTo(ROLE_ALREADY_HANDLED);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenRequestRoleWithAccessDenied_thenReturnAccessDenied() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP, false, (_, _, _) -> false))
						.as("createGroup with access denied gate").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole when access denied")
						.isEqualTo(ACCESS_DENIED);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE, null)).as("requestRole when access denied with null")
						.isEqualTo(ACCESS_DENIED);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenRequestRoleWithAccessGranted_thenReturnAccessGranted() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP, false, (_, _, _) -> true))
						.as("createGroup with granted access gate").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole when access granted").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, cgrDontExist(), null)).as("requestRole with cgrDontExist")
						.isEqualTo(SUCCESS);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenDefaultRole_thenReturnSuccess() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(getOrganization().isGroup(COMMUNITY, GROUP)).as("group exists").isTrue();
				assertThat(getOrganization().isRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER))
						.as("group manager role exists").isTrue();
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenRequestRoleWithNullArgs_thenHandleNullPointerException() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThatThrownBy(() -> requestRole(null, null, null)).as("requestRole(null, null, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> requestRole(null, null, null, null)).as("requestRole(null, null, null, null)")
						.isInstanceOf(NullPointerException.class);

				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThatThrownBy(() -> requestRole(COMMUNITY, null, null)).as("requestRole(COMMUNITY, null, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> requestRole(COMMUNITY, GROUP, null, null))
						.as("requestRole(COMMUNITY, GROUP, null, null)").isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> requestRole(null, GROUP, null)).as("requestRole(null, GROUP, null)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> requestRole(null, GROUP, ROLE)).as("requestRole(null, GROUP, ROLE)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> requestRole(null, null, ROLE)).as("requestRole(null, null, ROLE)")
						.isInstanceOf(NullPointerException.class);
				assertThatThrownBy(() -> requestRole(COMMUNITY, GROUP, null, new Object()))
						.as("requestRole(COMMUNITY, GROUP, null, new Object())").isInstanceOf(NullPointerException.class);
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenOnlyOneManager_thenReturnRoleAlreadyHandled() {
		runTest(new ThreadedTestAgent() {
			@Override
			public void behaviorInActivate() {
				assertThat(createGroup(COMMUNITY, GROUP)).as("createGroup return code").isEqualTo(SUCCESS);
				assertThat(requestRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER)).as("requestRole GROUP_MANAGER")
						.isEqualTo(ROLE_ALREADY_HANDLED);
				assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("requestRole ROLE").isEqualTo(SUCCESS);
				final ThreadedTestAgent helper = new ThreadedTestAgent() {
					@Override
					public void behaviorInActivate() {
						assertThat(requestRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER))
								.as("helper request GROUP_MANAGER should be denied").isEqualTo(ACCESS_DENIED);
						assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("helper request ROLE should be successful")
								.isEqualTo(SUCCESS);
						resume();
					}
				};

				final ThreadedTestAgent helper2 = new ThreadedTestAgent() {
					@Override
					public void behaviorInActivate() {
						assertThat(requestRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER))
								.as("helper2 request GROUP_MANAGER should be denied").isEqualTo(ACCESS_DENIED);
						assertThat(requestRole(COMMUNITY, GROUP, ROLE)).as("helper2 request ROLE should be successful")
								.isEqualTo(SUCCESS);
						resume();
					}
				};
				assertThat(launchAgent(helper)).as("launchAgent helper").isEqualTo(SUCCESS);
				assertThat(launchAgent(helper2)).as("launchAgent helper2").isEqualTo(SUCCESS);
				resume();
			}
		});
	}
}