package madkit.launching;

import static madkit.kernel.JunitMadkit.startTimer;
import static madkit.kernel.JunitMadkit.stopTimer;

import madkit.kernel.AbstractAgent;

/**
* @author Fabien Michel
*/
public class BucketBench extends AbstractAgent {

	@Override
	protected void activate() {
		getLogger().createLogFile();
		for (int i = 0; i < 30; i++) {
			startTimer();
			launchAgentBucket(AbstractAgent.class.getName(), 1000000);
			stopTimer("launch time ");
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		executeThisAgent(1,false);
	}

}
