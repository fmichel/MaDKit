package madkit.test.agents;

import madkit.kernel.GenericTestAgent;

/**
 *
 *
 */
public class ThreadedTestAgent extends GenericTestAgent {

	@Override
	protected void onLive() {
		orgInLive();
		behaviorInLive();
	}

	public static void main(String[] args) {
		executeThisAgent(1);
	}

}
