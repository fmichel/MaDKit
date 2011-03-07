/**
 * 
 */
package madkit.agentLifeCycle;

import java.util.ArrayList;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class  PerfTe extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 6155344570865964476L;
	AbstractAgent a;

	public void activate() {

		massLaunch();
	}


	/**
	 * 
	 */
	private void massLaunch() {
		if(logger != null){
			logger.info("\n******************* STARTING MASS LAUNCH *******************\n");
		}
//		for (int i = 0; i < 2000000; i++) {
//			if(i%100000==0){
//				System.err.println("launched "+i);
//				if (logger != null) {
//					logger.info("nb of launched agents " + i);
//				}
//			}
//			launchAgent(new test.madkit.agentLifeCycle.NormalAbstractLife());
//		}
//		JUnitBooter.stopTimer("old launch time = ");
//		System.err.println("done\n\n");
		a = launchAgent("madkit.kernel.AbstractAgent");
		a.createGroup("test", "group", false, null);
		System.err.println("begin");
		startTimer();
//		launchAgentBucket("madkit.agentLifeCycle.NormalAbstractLife", 3000100);
		launchAgentBucketWithRoles("madkit.kernel.AbstractAgent", 1000000,new ArrayList<String>());
		stopTimer("bucket launch time = ");
		System.err.println("done\n\n");
//		JUnitBooter.stopTimer("old launch time = "); // 6000000 min = 7s
//		startTimer();
//		ArrayList<AbstractAgent> agents = new ArrayList<AbstractAgent>(6000000);
//		for (int i = 6000000-1; i >=0 ; i--) {
//			if(i%1000000==0)
//				System.err.println("launched "+i);
//			agents.add(new AbstractAgent());
//		}
//		ArrayList<AbstractAgent> agents = new ArrayList<AbstractAgent>(6000000);
//		for (int i = 6000000-1; i >=0 ; i--) {
//			agents.add(new AbstractAgent());
//		}
//		stopTimer("old launch time = ");
	}
	
	/* (non-Javadoc)
	 * @see test.util.JUnitBooterAgent#madkitInit()
	 */
	@Override
	public void madkitInit() {
		String[] argss = {"--agentLogLevel","OFF","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--launchAgents",PerfTe.class.getName()};
		Madkit.main(argss);
	}
	
}