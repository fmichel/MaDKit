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
package madkit.simulation;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.AGENT_CRASH;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import madkit.kernel.Agent;
import madkit.kernel.MadkitConcurrentTestCase;

/**
 * The Class SimuAgentTest.
 */
public class SimuAgentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenSimuAgent_whenNotLaunchedByLauncher_thenAgentCrashes() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				SimuAgent sa = new SimuAgent();
				ReturnCode launchAgent = launchAgent(sa);
				threadAssertEquals(AGENT_CRASH, launchAgent);
				resume();
			}
		});
	}

	@Test
	public void givenSimuAgentTransitivelyLaunched_whenLaunch_thenSuccess() {
		runSimuTest(new SimuAgent() {
			@Override
			protected void onActivation() {
				SimuAgent sa = new SimuAgent();
				try {
					threadAssertEquals(SUCCESS, launchAgent(sa));
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
				resume();
			}
		});
	}

}