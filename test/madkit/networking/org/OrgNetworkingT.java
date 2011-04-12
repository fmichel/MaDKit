/**
 * 
 */
package madkit.networking.org;

import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.Roles;

import org.junit.Test;

import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class OrgNetworkingT extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -4991355687268788558L;
	String communityName = Roles.LOCAL_COMMUNITY;
	String groupName = Roles.NETWORK_GROUP;
	String roleName = "net agent";
	
	
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		assertEquals(SUCCESS,createGroup("other", "other", true,null));
		assertEquals(SUCCESS,requestRole("other", "other", "other",null));
		
		pause(1000);

		launchMKNetworkClient();
		pause(100000);
//		launchEmptyMKNetworkInstance();
			
		pause(1000);
			waitOther();
			//the group should be created on the other side
			//1. testing the org operations done on the other side
			assertNotNull(getAgentWithRole("public", "system", "site"));
			assertNotNull(getAgentWithRole("public", "system", "site2"));
			
			// 2
			waitOther();
			assertNull(getAgentWithRole("public", "system", "site2"));
			assertNotNull(getAgentWithRole("public", "system", "site"));
			assertEquals(getAgentsWithRole("public", "system", "site").size(),1);
			
			// 3
			waitOther();
			assertNull(getAgentWithRole("public", "system", "site"));
			assertNull(getAgentsWithRole("public", "system", "site"));
			
			// 4
			waitOther();
			assertNotNull(getAgentWithRole("public", "system", "site"));
			
			// 5
			waitOther();
//			assertTrue(isConnected());
			assertNull(getAgentWithRole("public", "system", "site"));
			
			// 6
			waitOther();
			assertNotNull(getAgentWithRole("public", "system", "site"));
			assertEquals(SUCCESS,requestRole("public", "system", "site",null));
			
			// 7
			waitOther();
			assertNull(getAgentWithRole("public", "system", "site"));
			assertEquals(ROLE_ALREADY_HANDLED,requestRole("public", "system", "site",null));
			
			
			pause(5000);
	}

	@Test
	public void madkitInit() {
		String[] args = {"--network","--autoAgentLogFile","bin/","--agentLogLevel","OFF","--MadkitLogLevel","OFF","--orgLogLevel","OFF","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}

	/**
	 * 
	 */
	private void waitOther() {
		while(nextMessage() == null){
			pause(200);
		}
	}
	
	public void launchMKNetworkClient() {
		new Thread(new Runnable() {			
			@Override
			public void run() {
				String[] args = {"--network","--agentLogLevel","ALL","--MadkitLogLevel","FINER","--orgLogLevel","OFF","--launchAgents","madkit.networking.org.MKClient"};
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
