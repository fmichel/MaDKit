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
			try {
				reloadAgentClass("madkit.classreloading.TestAgent");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			launchAgent("madkit.classreloading.TestAgent",true);
			pause(2000);
		}
	}
	
	public static void main(String[] argss) {
		String[] args = {"--agentLogLevel","ALL","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--launchAgents",Simple.class.getName()};
		Madkit.main(args);
	}
}