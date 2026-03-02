package madkit.testing.agents;

import madkit.kernel.DefaultTestAgent;

public class ThreadedTestAgent extends DefaultTestAgent {

	public ThreadedTestAgent() {
		super();
	}

	@Override
	protected void onLive() {
		try {
			orgInLive();
			behaviorInLive();
		} catch (Throwable e) {
			getMadkitConcurrentTestCase().threadFail(e);
		}
	}

}
