/**
 * 
 */
package madkit.boot.process;

import static org.junit.Assert.assertEquals;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.LevelOption;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class TestArg extends  JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -8343304647814729154L;

	@Override
	public void madkitInit() {
		String[] args = {"--autoAgentLogFile","bin","true","--agentLogLevel","ALL","--orgLogLevel","ALL","--MadkitLogLevel","ALL","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}
	
	@Override
	public void activate() {
		assertEquals("ALL",getMadkitProperty(LevelOption.agentLogLevel.toString()));
		assertEquals("bin/mkLogFile",getMadkitProperty(Madkit.MadkitLogFile));
		assertEquals("ALL",getMadkitProperty(LevelOption.madkitLogLevel.toString()));
	}
}