/**
 * 
 */
package madkit.networking;

import madkit.kernel.AgentAddress;
import madkit.kernel.Madkit;
import madkit.kernel.Message;

import org.junit.Test;
/**
 * @author fab
 *
 */
public class DeconnectionMKClient extends AbstractNetworkingTest{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3688794881530061622L;
	private AgentAddress other;
	
	
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		pause(2000);
//		stopNetwork();

	}

	/**
	 * 
	 */
	private void launchServerSideTest() {
		testAgent.sendMessage(other, new Message());
		pause(1000);
	}
	
	@Test
	public void madkitInit() {
		String[] args = {"--network","--agentLogLevel","OFF","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--booterClass",getClass().getName()};
		Madkit.main(args);
	}


}
