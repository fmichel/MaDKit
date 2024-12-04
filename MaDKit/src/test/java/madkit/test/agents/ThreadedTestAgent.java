package madkit.test.agents;

import madkit.kernel.GenericTestAgent;

/**
 * @author Fabien Michel
 *
 */
public class ThreadedTestAgent extends GenericTestAgent {

	@Override
	protected void onLiving() {
		orgInLive();
		behaviorInLive();
	}

	public static void main(String[] args) {
		executeThisAgent(1);
	}

}
