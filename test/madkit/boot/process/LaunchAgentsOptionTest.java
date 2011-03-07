/**
 * 
 */
package madkit.boot.process;

import static org.junit.Assert.fail;
import madkit.kernel.Madkit;

import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class LaunchAgentsOptionTest extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 356877495113428719L;

	@Override
	public void madkitInit() {
		String[] args = {"--"+Madkit.warningLogLevel,"INFO",
				"--"+Madkit.agentLogLevel,"INFO",
				"--"+Madkit.MadkitLogLevel,"INFO",
				"--"+Madkit.launchAgents,getClass().getName()+",false,2;madkit.kernel.AbstractAgent,true,a"
		};
		try {
			Madkit.main(args);
		} catch (Throwable e) {
			System.err.println("\n\n\n------------------------------------");
			while(e.getCause() != null)
				e = e.getCause();
			e.printStackTrace();
			System.err.println("------------------------------------\n\n\n");
			fail(getClass().getSimpleName());
		}
	}
	
	/* (non-Javadoc)
	 * @see test.util.JUnitBooterAgent#activate()
	 */
	@Override
	protected void activate() {
		if(logger != null)
			logger.info("launched");
	}
}


