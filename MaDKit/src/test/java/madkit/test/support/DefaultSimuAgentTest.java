package madkit.test.support;

import madkit.kernel.Agent;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.TestAgentSupport;
import madkit.simulation.SimuAgent;

public class DefaultSimuAgentTest extends SimuAgent implements TestAgentSupport {
	private MadkitConcurrentTestCase madkitConcurrentTestCase;

	@Override
	public Agent getAgent() {
		return this;
	}

	@Override
	public MadkitConcurrentTestCase getMadkitConcurrentTestCase() {
		return madkitConcurrentTestCase;
	}

	@Override
	public void setMadkitConcurrentTestCase(MadkitConcurrentTestCase mdkitConcurrentTestCase) {
		this.madkitConcurrentTestCase = mdkitConcurrentTestCase;
	}

	@Override
	protected void onActivation() {
		try {
			orgInActivate();
			behaviorInActivate();
		} catch (Throwable e) {
			if (madkitConcurrentTestCase != null) {
				madkitConcurrentTestCase.threadFail(e);
			}
		}
	}

	@Override
	protected void onEnd() {
		try {
			orgInEnd();
			behaviorInEnd();
			resume();
		} catch (Throwable e) {
			if (madkitConcurrentTestCase != null) {
				madkitConcurrentTestCase.threadFail(e);
			}
		}
	}

	public void resume() {
		madkitConcurrentTestCase.resume();
	}

}
