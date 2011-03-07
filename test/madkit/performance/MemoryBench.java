/**
 * 
 */
package madkit.performance;

import java.util.Arrays;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;

/**
 * @author fab
 *
 */
public class MemoryBench extends AbstractAgent {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -8973359349586637788L;

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		System.err.println(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
		createGroup("comm", "group");
		launchAgentBucketWithRoles("madkit.kernel.AbstractAgent", 4000000, Arrays.asList("comm", "group","role"));
		System.err.println(Runtime.getRuntime().totalMemory()-Runtime.getRuntime().freeMemory());
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String[] argss = {"--agentLogLevel","OFF","--"+Madkit.MadkitLogLevel,"OFF","--orgLogLevel","OFF","--launchAgents",MemoryBench.class.getName(),",false"};
		Madkit.main(argss);		
	}

}