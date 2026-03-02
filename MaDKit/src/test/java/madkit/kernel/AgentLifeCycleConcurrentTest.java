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

import org.testng.annotations.Test;

import madkit.testing.agents.BugInActivateAgent;
import madkit.testing.agents.BugInEndThreadedAgent;
import madkit.testing.agents.BugInLiveAgent;
import madkit.testing.agents.BugInLiveAndEndAgent;
import madkit.testing.agents.ThreadedTestAgent;

public class AgentLifeCycleConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenBugInActivateAgent_whenLaunchAgent_thenReturnCrash() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				DefaultTestAgent a = new BugInActivateAgent() {
					@Override
					protected void onActivation() {
						behaviorInActivate();
					}
				};
				assertThat(launchAgent(a)).as("launchAgent return code").isEqualTo(ReturnCode.AGENT_CRASH);
				checkTermination(a);
				assertThat(a.didPassThroughEnd()).as("agent did not pass through end").isFalse();
				resume();
			}
		});
	}

	@Test
	public void givenBugInLiveAgent_whenLaunchAgent_thenTerminate() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				DefaultTestAgent a = new BugInLiveAgent();
				assertThat(launchAgent(a)).as("launchAgent return code").isEqualTo(ReturnCode.SUCCESS);
				awaitTermination(a, 1000);
				assertThat(a.didPassThroughEnd()).as("agent passed through end").isTrue();
				resume();
			}
		});
	}

	@Test
	public void givenBugInLiveAndEndAgent_whenLaunchAgent_thenTerminate() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				DefaultTestAgent a = new BugInLiveAndEndAgent();
				assertThat(launchAgent(a)).as("launchAgent return code").isEqualTo(ReturnCode.SUCCESS);
				awaitTermination(a, 1000);
				assertThat(a.didPassThroughEnd()).as("agent passed through end").isTrue();
				resume();
			}
		});
	}

	@Test
	public void givenBugInEndThreadedAgent_whenLaunchAgent_thenTerminate() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				DefaultTestAgent a = new BugInEndThreadedAgent();
				assertThat(launchAgent(a)).as("launchAgent return code").isEqualTo(ReturnCode.SUCCESS);
				awaitTermination(a, 1000);
				assertThat(a.didPassThroughEnd()).as("agent passed through end").isTrue();
				resume();
			}
		});
	}

	@Test
	public void givenThreadedTestAgent_whenLaunchAgent_thenTerminate() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				DefaultTestAgent a = new ThreadedTestAgent();
				assertThat(launchAgent(a)).as("launchAgent return code").isEqualTo(ReturnCode.SUCCESS);
				awaitTermination(a, 1000);
				assertThat(a.didPassThroughEnd()).as("agent passed through end").isTrue();
				resume();
			}
		});
	}

	@Test
	public void givenGenericTestAgent_whenLaunchAgent_thenNominal() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				DefaultTestAgent a = new DefaultTestAgent();
				assertThat(launchAgent(a)).as("launchAgent return code").isEqualTo(ReturnCode.SUCCESS);
				assertThat(a.didPassThroughEnd()).as("agent did not pass through end").isFalse();
				assertThat(((Agent) a).alive.get()).as("agent is alive").isTrue();
				resume();
			}
		});
	}
}