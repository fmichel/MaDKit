package madkit.simulation;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.assertj.core.api.Assertions.fail;

import org.testng.annotations.Test;

import madkit.kernel.MadkitUnitTestCase;

public class SimuAgentTest extends MadkitUnitTestCase {

	@Test
	public void givenSimuAgent_whenNotLaunchedByLauncher_thenThrowIllegalStateException() {
		try {
			new SimuAgent().launchAgent(new SimuAgent());
			fail();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	@Test
	public void givenSimuAgentTransitivelyLaunched_whenLaunch_thenSuccess() {
		launchSimuAgentTest(new SimuAgent() {
			@Override
			protected void onActivation() {
				SimuAgent sa = new SimuAgent();
				try {
					threadAssertEquals(SUCCESS, launchAgent(sa));
				} catch (IllegalStateException e) {
					e.printStackTrace();
				}
			}
		});
	}

}