/**
 * 
 */
package madkit.classreloading;


import org.junit.Test;

import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class Simple extends Agent {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6296623449623724485L;

	/* (non-Javadoc)
	 * @see test.util.JUnitBooterAgent#activate()
	 */
	@Override
	public void activate() {
		while (true) {
			reloadAgentClass("madkit.classreloading.TestAgent");
			launchAgent("madkit.classreloading.TestAgent");
			pause(3000);
		}
	}
	
	public static void main(String[] argss) {
		String[] args = {"--agentLogLevel","OFF","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--launchAgents",Simple.class.getName()};
		Madkit.main(args);
	}
}