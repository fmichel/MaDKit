package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.UnstopableAgent;

import org.junit.Test;

public class KillUnstoppableAgentTest extends JunitMadkit {

	@Test
	public void killUnstoppableInActivate() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true);
				unstopableAgent.setLogLevel(Level.ALL);
				startTimer();
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				stopTimer("launch time out ");
				assertEquals(SUCCESS, killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
			}
		});
	}

	@Test
	public void brutalKillUnstoppableInActivateAndLive() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true, true, false);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(SUCCESS, killAgent(unstopableAgent, 0));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
	}

	@Test
	public void brutalKillUnstoppableInActivateAndLiveAndEnd() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true, true, true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(TIMEOUT, killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
	}

	@Test
	public void brutalKillUnstoppableUsingSelfRef() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				assertEquals(SUCCESS, unstopableAgent.killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
				unstopableAgent = new UnstopableAgent(true, false, true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				assertEquals(TIMEOUT, unstopableAgent.killAgent(unstopableAgent, 1));
				assertAgentIsTerminated(unstopableAgent);
				pause(1000);
			}
		});
	}

}
