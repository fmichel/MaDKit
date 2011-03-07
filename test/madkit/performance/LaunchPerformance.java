/**
 * 
 */
package madkit.performance;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class LaunchPerformance extends JUnitBooterAgent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3751374630788416913L;

	/**
	 * 
	 */
	private void massLaunch() {
		AbstractAgent a;
		if(logger != null){
			logger.info("\n******************* STARTING MASS LAUNCH *******************\n");
		}
		a = launchAgent("madkit.kernel.AbstractAgent",0);
		a.createGroup("test", "group", false, null);
		setAgentLogLevel(Level.OFF);
		setMadkitLogLevel(Level.OFF);
//		System.err.println("begin");
//		for (int i = 0; i < 2000000; i++) {
//			if(i%100000==0){
//				System.err.println("launched "+i);
//				if (logger != null) {
//					logger.info("nb of launched agents " + i);
//				}
//			}
//			launchAgent(new test.madkit.agentLifeCycle.NormalAbstractLife());
//		}
//		System.err.println("done\n\n");
		startTimer();
		System.err.println("begin");
		launchAgentBucket("madkit.agentLifeCycle.NormalAbstractLife", 30100);
//		launchAgentBucket("madkit.kernel.AbstractAgent", 6000100);
		stopTimer("done");
	}

}
