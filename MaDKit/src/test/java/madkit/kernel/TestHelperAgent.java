package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

import java.util.logging.Level;

/**
 *
 *
 */
public class TestHelperAgent extends Agent {

	private boolean goThroughEnd = false;

	/**
	 * 
	 */
	public TestHelperAgent() {
		getLogger().setLevel(Level.ALL);
	}

	protected void blockForever() {
		try {
			Object o = new Object();
			synchronized (o) {
				getLogger().info(() -> "blocking myself " + Thread.currentThread());
				o.wait();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			throw new AgentInterruptedException();
		}
	}

	protected void computeForEver() {
		for (int i = 0; i < Integer.MAX_VALUE; i++) {
			Math.cos(Math.random());
			if (i % 1000000 == 0)
				getLogger().info("computing... step " + i);
		}
	}

	protected void bug() {
		Object o = null;
		o.toString();
	}

	public void createDefaultCGR() {
		createGroup(COMMUNITY, GROUP, false, null);
		assertEquals(requestRole(COMMUNITY, GROUP, ROLE, null), SUCCESS);
	}

	protected void checkTermination() {
		assertFalse(alive.get());
		assertEquals(kernel, KernelAgent.deadKernel);
	}

	protected void replyToLastReiceivedMessage() {
		Message m = waitNextMessage();
		reply(new Message(), m);
	}

	@Override
	protected void onEnd() {
		goThroughEnd = true;
	}

	public boolean didPassThroughEnd() {
		return goThroughEnd;
	}

}
