/**
 * 
 */
package madkit.boot.process;

import static org.junit.Assert.assertEquals;

import java.io.File;

import madkit.kernel.Madkit;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class TestArg2 extends  JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5455611923068477398L;
	@Override
	public void madkitInit() {
		String[] args = {"--launchAgents",getClass().getName(),"--autoAgentLogFile","bin/","true","--agentLogLevel","ALL","--network","--orgLogLevel","ALL","--MadkitLogLevel","ALL"};
		Madkit.main(args);
	}
	@Override
	public void activate() {
		setMadkitProperty(Madkit.MadkitLogFile, "bin/mklogFileTest");
		assertEquals("bin/mklogFileTest",getMadkitProperty(Madkit.MadkitLogFile));
		assertEquals("ALL",getMadkitProperty(Madkit.agentLogLevel));
		assertEquals("ALL",getMadkitProperty(Madkit.MadkitLogLevel));
	}
}


