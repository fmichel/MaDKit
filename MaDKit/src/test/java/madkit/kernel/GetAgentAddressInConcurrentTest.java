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

import madkit.test.agents.CGRAgent;

/**
 *
 * @version 6.0.2
 * 
 */
public class GetAgentAddressInConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgent_whenGetAgentAddress_thenSuccess() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				threadAssertNotNull(getOrganization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenLeaveRole_thenAgentAddressIsNull() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				AgentAddress aa = getOrganization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this);
				threadAssertNotNull(aa);
				threadAssertTrue(aa.isValid());
				leaveRole(COMMUNITY, GROUP, ROLE);
				threadAssertFalse(aa.isValid());
				threadAssertFalse(getOrganization().isRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenAgent_whenLeaveGroup_thenAgentAddressIsNull() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				AgentAddress aa = getOrganization().getRole(COMMUNITY, GROUP, ROLE).getAgentAddressOf(this);
				threadAssertNotNull(aa);
				threadAssertTrue(aa.isValid());
				leaveGroup(COMMUNITY, GROUP);
				threadAssertFalse(aa.isValid());
				threadAssertFalse(getOrganization().isGroup(COMMUNITY, GROUP));
				resume();
			}
		});
	}

	@Test
	public void givenNullCommunity_whenGetAnyAgentAddress_thenThrowsNullPointerException() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				try {
					getOrganization().getGroup(null, GROUP).getAnyAgentAddressOf(this);
					noExceptionFailure();
				} catch (NullPointerException e) {
					resume();
				}
			}
		});
	}

	@Test
	public void givenNullGroup_whenGetAgentAddress_thenThrowsNullPointerException() {
		runTest(new CGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				try {
					threadAssertNotNull(getOrganization().getRole(COMMUNITY, null, ROLE).getAgentAddressOf(this));
					noExceptionFailure();
				} catch (NullPointerException e) {
					resume();
				}
			}
		});
	}

}
