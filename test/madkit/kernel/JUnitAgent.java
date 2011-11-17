package madkit.kernel;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;

public class JUnitAgent extends AbstractAgent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public void successOnLaunch(AbstractAgent a) {
		assertEquals(SUCCESS, launchAgent(a));
	}

}
