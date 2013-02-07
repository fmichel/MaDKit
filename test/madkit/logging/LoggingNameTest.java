package madkit.logging;

import static org.junit.Assert.*;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NormalAA;

import org.junit.Test;


public class LoggingNameTest extends JunitMadkit {

	@SuppressWarnings("serial")
	@Test
	public void setNameTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				NormalAA a;
				assertEquals(ReturnCode.SUCCESS, launchAgent(a = new NormalAA()));
				assertEquals("["+NormalAA.class.getSimpleName()+"-"+a.hashCode()+"]", a.getLogger().getName());
			}
		});
	}

}
