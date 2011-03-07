/**
 * 
 */
package madkit.classreloading;

import java.util.logging.Level;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.Madkit;
import madkit.kernel.Scheduler;

public class TestAgent extends Agent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	public TestAgent() {
		// TODO Auto-generated constructor stub
	}
	
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		setLogLevel(Level.ALL);
		super.activate();
		if(logger != null)
			logger.info("\n\ndS qsdfqsdfd ydi\n\n");
	}
	
	/**
	 * 
	 */
	@Override
	protected void live() {
		while(true){
			pause(2000);
			if(logger != null)
				logger.info("infodd");
			Scheduler s = new Scheduler();
			launchAgent(s);
		}

	}
	
	public static void main(String[] argss) {
		String[] args = {"--agentLogLevel","INFO","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--launchAgents",TestAgent.class.getName()+",true"};
		Madkit.main(args);
	}
}