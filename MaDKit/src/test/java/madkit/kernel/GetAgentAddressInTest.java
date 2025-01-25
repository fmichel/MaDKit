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

import madkit.kernel.Agent.ReturnCode;
import madkit.test.agents.CGRAgent;

/**
 *
 * @since MaDKit 5.0.0.15
 * @version 0.9
 * 
 */
public class GetAgentAddressInTest extends JunitMadkit {

	/**
	 * Success.
	 */
	@Test
	public void success() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				threadAssertNotNull(getOrganization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this));
			}
		});
	}

	/**
	 * Null after leave role.
	 */
	@Test
	public void nullAfterLeaveRole() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				AgentAddress aa = getOrganization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this);
				threadAssertNotNull(aa);
				threadAssertTrue(aa.isValid());
				leaveRole(COMMUNITY, GROUP, ROLE);
				threadAssertFalse(aa.isValid());
				threadAssertFalse(getOrganization().isRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	/**
	 * Null after leave group.
	 */
	@Test
	public void nullAfterLeaveGroup() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				AgentAddress aa = getOrganization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this);
				threadAssertNotNull(aa);
				threadAssertTrue(aa.isValid());
				leaveGroup(COMMUNITY, GROUP);
				threadAssertFalse(aa.isValid());
				threadAssertFalse(getOrganization().isGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void nullCommunity() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				try {
					AgentAddress aa = getOrganization().getGroup(null, GROUP).getAnyAgentAddressOf(this);
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void nullGroup() {
		launchTestedAgent(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				try {
					threadAssertNotNull(getOrganization().getRole(COMMUNITY, null, ROLE).getAgentAddressOf(this));
					noExceptionFailure();
				} catch (NullPointerException e) {
					throw e;
				}
			}
		}, ReturnCode.AGENT_CRASH);
	}

}
