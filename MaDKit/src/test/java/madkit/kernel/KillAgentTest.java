
package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.NOT_YET_LAUNCHED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.kernel.Agent.ReturnCode.TIMEOUT;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

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
 * @version 0.9
 * 
 */

public class KillAgentTest extends JunitMadkit {

	@Test
	public void killAgent() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new GenericTestAgent();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a), SUCCESS);
				assertTrue(a.didPassThroughEnd());
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killThreadedAgent() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentPausedInLive();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a), SUCCESS);
				assertTrue(a.didPassThroughEnd());
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killAgentBlockedInEnd() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new AgentBlockedInEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a, 3), SUCCESS);
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killThreadedAgentBlockedInLive() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentBlockedInLive();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a, 1), SUCCESS);
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killThreadedAgentBlockedInEnd() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentBlockedInEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a, 1), SUCCESS);
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killThreadedAgentBlockedInLiveAndEnd() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentBlockedInLiveAndEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a, 2), SUCCESS);
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killAgentForeverInEnd() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new AgentForeverInEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a, 1), SUCCESS);
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killThreadedAgentForeverInEnd() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentForeverInEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a, 1), SUCCESS);
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killThreadedAgentForeverInLive() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentForeverInLive();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a, 1), SUCCESS);
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void killThreadedAgentForeverInLiveAndEnd() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedAgentForeverInLiveAndEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertTrue(a.alive.get());
				assertEquals(killAgent(a, 1), SUCCESS);
				a.checkTermination();
			}
		}, SUCCESS);
	}

	@Test
	public void notYetActivated() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				ThreadedAgentBlockedInActivate a = new ThreadedAgentBlockedInActivate();
				assertEquals(launchAgent(a, 1), TIMEOUT);
				assertEquals(killAgent(a, 1), NOT_YET_LAUNCHED);
			}
		}, SUCCESS);
	}

	@Test
	public void firedFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new RequestRoleAgent();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	// No sens for now
//	@Test
//	public void cgrBlockedInActivateFiredFromOrganization() {
//		launchTestedAgent(new Agent() {
//			@Override
//			protected void activate() {
//				GenericTestAgent a = new CGRBlockedInActivate();
//				assertEquals(launchAgent(a,true,1), TIMEOUT);
//				assertNotNull(getAgentWithRole(COMMUNITY,GROUP,ROLE));
//				assertEquals(killAgent(a, 1), SUCCESS);
//				assertNull(getAgentWithRole(COMMUNITY,GROUP,ROLE));
//			}
//		}, SUCCESS);
//	}

	@Test
	public void cgrBlockedInLiveFiredFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRBlockedInLive();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void cgrBlockedInEndFiredFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRBlockedInEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void threadedCgrBlockedInEndFiredFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedCGRBlockedInEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void cgrBlockedInLiveAndEndFiredFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRBlockedInLiveAndEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void cgrForeverInLiveFiredFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRForeverInLive();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void cgrForeverInEndFiredFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRForeverInEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void threadedCgrForeverInEndFiredFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new ThreadedCGRForeverInEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

	@Test
	public void cgrForeverInLiveAndEndFiredFromOrganization() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				GenericTestAgent a = new CGRForeverInLiveAndEnd();
				assertEquals(launchAgent(a), SUCCESS);
				assertNotNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
				assertEquals(killAgent(a, 1), SUCCESS);
				assertNull(getAgentWithRole(COMMUNITY, GROUP, ROLE));
			}
		}, SUCCESS);
	}

}
