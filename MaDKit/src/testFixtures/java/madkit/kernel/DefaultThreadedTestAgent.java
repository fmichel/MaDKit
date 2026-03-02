package madkit.kernel;

public class DefaultThreadedTestAgent extends DefaultTestAgent {

	@Override
	protected void onLive() {
		try {
			orgInLive();
			behaviorInLive();
		} catch (Throwable e) {
			if (getMadkitConcurrentTestCase() != null) {
				getMadkitConcurrentTestCase().threadFail(e);
			}
		}
	}

}
