package madkit.kernel;

import java.util.logging.Level;

import org.testng.annotations.Test;

import madkit.action.KernelAction;

public class NetworkAgentTest extends MadkitConcurrentTestCase {

	@Override
	protected String[] getMadkitTestArgs() {
		return new String[] { "--network" };
	}

//	@Test
	public void givenNetworkAgent_whenConnected_then() {
		runTest(new ConcurrentTestAgent() {
			@Override
			protected void onActivation() {
				getLogger().setLevel(Level.ALL);
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance(getDefaultMadkitArgs());
				pause(1000);
				otherMK.assertNetworkStatus(false);
				madkit.doAction(KernelAction.STOP_NETWORK);
//				waitNextMessage(5000);
				resume();
			}
		});
	}

	@Test
	public void givenNetworkOn_whenStop_thenStatusOff() {
		runTest(new ConcurrentTestAgent() {
			@Override
			public void behaviorInActivate() {
				getLogger().setLevel(Level.ALL);
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance(getDefaultMadkitArgs());
//				pause(1000);
				otherMK.assertNetworkStatus(true);
				madkit.doAction(KernelAction.STOP_NETWORK);
				waitNextMessage(1000);
				otherMK.assertNetworkStatus(false);
				resume();
			}
		});
	}

}
