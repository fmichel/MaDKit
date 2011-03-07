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
public class TestArg3 extends  JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4532769853173612100L;

	@Override
	public void madkitInit() {
		Madkit.main(null);
	}
	@Override
	public void activate() {
	}
	
	@Test
	public void oneArg(){
			String[] args = {"--network","--launchAgents",getClass().getName()};
			Madkit.main(args);
	}
}


