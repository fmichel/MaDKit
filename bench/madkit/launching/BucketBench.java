package madkit.launching;

import java.io.File;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit.Option;
import static madkit.kernel.JunitMadkit.*;

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
