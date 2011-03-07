/**
 * 
 */
package madkit.boot.process;

import madkit.kernel.Madkit;

import org.junit.Test;

import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class TestArgMultipleLaunch extends  JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7452900945215040830L;

	@Override
	public void madkitInit() {
		Madkit.main(null);
	}
	@Override
	public void activate() {
	}

	@Test
	public void oneArg(){
		String[] args = {"--MadkitLogLevel","ALL","--launchAgents",getClass().getName()+";madkit.kernel.AbstractAgent,true,3"};
		Madkit.main(args);
	}
}


