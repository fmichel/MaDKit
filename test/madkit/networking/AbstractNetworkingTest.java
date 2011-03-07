/**
 * 
 */
package madkit.networking;

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static org.junit.Assert.assertEquals;
import madkit.kernel.Madkit;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class AbstractNetworkingTest extends JUnitBooterAgent{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 8392880042525420730L;

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		assertEquals(SUCCESS,testAgent.createGroup("other", "other", true,null));
		assertEquals(SUCCESS,testAgent.requestRole("other", "other", "other",null));
	}

	/**
	 * 
	 */
	public void waitOther() {
		while(testAgent.nextMessage() == null){
			pause(100);
		}
	}
	
	public void launchMKNetworkInstance(final String booterClass) {
		new Thread(new Runnable() {			
			@Override
			public void run() {
				String[] args = {"--agentLogLevel","ALL","--MadkitLogLevel","ALL","--orgLogLevel","ALL","--network","--booterClass",booterClass};
				Madkit.main(args);
		}
	}).start();

	}

	public void launchEmptyMKNetworkInstance() {
		new Thread(new Runnable() {			
			@Override
			public void run() {
				String[] args = {"--network","--agentLogLevel","OFF","--MadkitLogLevel","OFF","--orgLogLevel","OFF"};
				Madkit.main(args);
				pause(5000);
		}
	}).start();
	}

}
