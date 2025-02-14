
package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.kernel.Agent.ReturnCode.TIMEOUT;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

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

public class LaunchAgentTest extends MadkitUnitTestCase {

	@Test
	public void returnSuccessOnLaunchNormalAgent() {
		Agent a = new EmptyAgent();
		assertFalse(a.alive.get());
		launchTestedAgent(a, SUCCESS);
		assertTrue(a.alive.get());
	}

	@Test
	public void returnSuccessOnLaunchThreadedAgent() {
		Agent a = new ThreadedAgentBlockedInLive();
		assertFalse(a.alive.get());
		launchTestedAgent(a, SUCCESS);
		assertTrue(a.alive.get());
	}

	@Test
	public void returnSuccessOnLaunchThreadedAgentV2() {
		Agent a = new ThreadedAgentBlockedInLive();
		assertFalse(a.alive.get());
		launchTestedAgent(a, SUCCESS);
		assertTrue(a.alive.get());
	}

	@Test
	public void returnTimeOutOnLaunchThreadedAgentBlockedInActivate() {
		Agent a = new ThreadedAgentBlockedInActivate();
		assertFalse(a.alive.get());
		launchTestedAgent(a, TIMEOUT, false, 1, 10);
		assertFalse(a.alive.get());
	}
}
