package madkit.test.utils;

import madkit.kernel.MadkitConcurrentTestCase;

public class MadkitNetworkTestCase extends MadkitConcurrentTestCase {

	@Override
	protected String[] getMadkitTestArgs() {
		return new String[] { "--network" };
	}

	@Override
	public void resume() {
		super.resume();
	}

}
