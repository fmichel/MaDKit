/**
 * 
 */
package madkit.logging;

import static org.junit.Assert.fail;

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class WarningLogLevelTest extends JUnitBooterAgent{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -5435128072383476445L;
	AbstractAgent other = new AbstractAgent();
	AbstractAgent other2 = new AbstractAgent();
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		if(logger != null)
			logger.info("test");
		createGroup("test", "test");
		createGroup("test", "test");
		sendMessage(null, null);
	}

	@Test
	public void madkitInit() {
		String[] args = {"--"+Madkit.warningLogLevel,"INFO",
				"--"+Madkit.agentLogLevel,"FINE",
				"--"+Madkit.warningLogLevel ,"FINE",
				"--"+Madkit.MadkitLogLevel,"OFF",
				"--orgLogLevel","OFF",
				"--"+Madkit.launchAgents,getClass().getName()
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

}
