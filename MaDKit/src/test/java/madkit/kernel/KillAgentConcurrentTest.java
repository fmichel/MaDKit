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

import static madkit.kernel.Agent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.kernel.Agent.ReturnCode.TIMEOUT;

import madkit.test.agents.AgentBlockedInEnd;
import madkit.test.agents.AgentForeverInEnd;
import madkit.test.agents.CGRBlockedInEnd;
import madkit.test.agents.CGRBlockedInLive;
import madkit.test.agents.CGRBlockedInLiveAndEnd;
import madkit.test.agents.CGRForeverInEnd;
import madkit.test.agents.CGRForeverInLive;
import madkit.test.agents.CGRForeverInLiveAndEnd;
import madkit.test.agents.RequestRoleAgent;
import madkit.test.agents.ThreadedAgentBlockedInActivate;
import madkit.test.agents.ThreadedAgentBlockedInEnd;
import madkit.test.agents.ThreadedAgentBlockedInLive;
import madkit.test.agents.ThreadedAgentBlockedInLiveAndEnd;
import madkit.test.agents.ThreadedAgentForeverInEnd;
import madkit.test.agents.ThreadedAgentForeverInLive;
import madkit.test.agents.ThreadedAgentForeverInLiveAndEnd;
import madkit.test.agents.ThreadedAgentPausedInLive;
import madkit.test.agents.ThreadedCGRBlockedInEnd;
import madkit.test.agents.ThreadedCGRForeverInEnd;

/**
 *
 * @since MaDKit 6
 * @version 6.0.2
 * 
 */

public class KillAgentConcurrentTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgent_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new GenericTestAgent();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a), SUCCESS);
				threadAssertTrue(a.didPassThroughEnd());
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgent_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentPausedInLive();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a), SUCCESS);
				threadAssertTrue(a.didPassThroughEnd());
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenAgentBlockedInEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new AgentBlockedInEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a, 3), SUCCESS);
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgentBlockedInLive_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentBlockedInLive();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgentBlockedInEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentBlockedInEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgentBlockedInLiveAndEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentBlockedInLiveAndEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a, 2), SUCCESS);
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenAgentForeverInEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new AgentForeverInEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgentForeverInEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentForeverInEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgentForeverInLive_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentForeverInLive();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenThreadedAgentForeverInLiveAndEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentForeverInLiveAndEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertTrue(a.alive.get());
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				checkTermination(a);
				resume();
			}
		});
	}

	@Test
	public void givenNotYetActivatedAgent_whenKillAgent_thenNotYetLaunched() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				ThreadedAgentBlockedInActivate a = new ThreadedAgentBlockedInActivate();
				threadAssertEquals(launchAgent(a, 1), TIMEOUT);
				threadAssertEquals(killAgent(a, 1), NOT_YET_LAUNCHED);
				resume();
			}
		});
	}

	@Test
	public void givenAgentFiredFromOrganization_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new RequestRoleAgent();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenCgrBlockedInLive_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRBlockedInLive();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenCgrBlockedInEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRBlockedInEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenThreadedCgrBlockedInEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedCGRBlockedInEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenCgrBlockedInLiveAndEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRBlockedInLiveAndEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenCgrForeverInLive_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRForeverInLive();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenCgrForeverInEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRForeverInEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenThreadedCgrForeverInEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedCGRForeverInEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}

	@Test
	public void givenCgrForeverInLiveAndEnd_whenKillAgent_thenSuccess() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRForeverInLiveAndEnd();
				threadAssertEquals(launchAgent(a), SUCCESS);
				threadAssertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(killAgent(a, 1), SUCCESS);
				threadAssertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				resume();
			}
		});
	}
}