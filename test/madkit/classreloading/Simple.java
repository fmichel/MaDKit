/**
 * 
 */
package madkit.classreloading;


import java.util.logging.Level;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
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
		setLogLevel(Level.ALL);
		launchAgent("madkit.kernel.AbstractAgent",true);
		while (true) {
			try {
				reloadAgentClass("madkit.classreloading.TestAgent");
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			AbstractAgent a = launchAgent("madkit.kernel.AbstractAgent",true);
			pause(2000);
				killAgent(a);
		}
	}
	
	public static void main(String[] argss) {
		String[] args = {"--agentLogLevel","ALL","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--launchAgents",Simple.class.getName()};
		Madkit.main(args);
	}
}