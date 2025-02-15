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

import madkit.test.agents.EmptyAgent;
import madkit.test.agents.ThreadedAgentBlockedInActivate;
import madkit.test.agents.ThreadedAgentBlockedInLive;

/**
 *
 * @since MaDKit 6
 * @version 0.9
 * 
 */

public class LaunchAgentConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenNormalAgent_whenLaunchAgent_thenReturnSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				Agent a = new EmptyAgent();
				threadAssertFalse(a.alive.get());
				launchAgent(a);
				threadAssertTrue(a.alive.get());
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgent_whenLaunchAgent_thenReturnSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				Agent a = new ThreadedAgentBlockedInLive();
				threadAssertFalse(a.alive.get());
				launchAgent(a);
				threadAssertTrue(a.alive.get());
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgentBlockedInActivate_whenLaunchAgent_thenReturnTimeOut() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				Agent a = new ThreadedAgentBlockedInActivate();
				threadAssertFalse(a.alive.get());
				ReturnCode r = launchAgent(a, 1);
				threadAssertFalse(a.alive.get());
				threadAssertEquals(ReturnCode.TIMEOUT, r);
				resume();
			}
		});

	}
}
