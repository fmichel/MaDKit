/**
 * 
 */
package madkit.boot.process;

import madkit.kernel.Madkit;

import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class NullArg extends  JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -44315596476812753L;

	@Override
	public void madkitInit() {
		Madkit.main(null);
	}
}


