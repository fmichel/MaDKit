/**
 * 
 */
package madkit.boot.process;

import madkit.kernel.Agent;
import madkit.kernel.Madkit;

/**
 * @author fab
 *
 */
public class BootAndQuitTest extends  Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3420708339715168399L;

	public static void main(String[] args) {
		String[] argss = {"--autoAgentLogFile","bin;true","--agentLogLevel","ALL","--orgLogLevel","ALL","--MadkitLogLevel","ALL","--launchAgents",BootAndQuitTest.class.getName()};
		Madkit.main(argss);
	}
	
	@Override
	public void live() {
		for (int i = 0; i < 5; i++) {
			launchAgent(new Agent());
			pause(500);
			if(logger != null)
				logger.info("living");
		}
	}
}


