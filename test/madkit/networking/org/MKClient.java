/**
 * 
 */
package madkit.networking.org;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
/**
 * @author fab
 *
 */
public class MKClient extends Agent{

	/**
	 * 
	 */
	private static final long serialVersionUID = -371337965704044173L;
	String communityName = LocalCommunity.NAME;
	String groupName = Groups.NETWORK;
	String roleName = "net agent";
	private AgentAddress other;
	
	
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		if(logger != null)
			logger.info("\n\n-----------Client launched ------------\n\n");
		assertEquals(SUCCESS,createGroup("other", "other", true,null));
		assertEquals(SUCCESS,requestRole("other", "other", "client",null));

		pause(1000);
		/////////////////////////// REQUEST ROLE ////////////////////////
		other = getAgentWithRole("other", "other", "other");
		assertNotNull(other);
		ReturnCode code = createGroup("public", "system", true,null);
		assertEquals(SUCCESS,code);
		code = requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);
		code = requestRole("public", "system", "site2",null);
		assertEquals(SUCCESS,code);

		launchServerSideTest();

		// 2
		code = leaveRole("public", "system", "site2");
		assertEquals(SUCCESS,code);
		launchServerSideTest();

		// 3
		assertEquals(SUCCESS,leaveRole("public", "system", "site"));
		launchServerSideTest();

		// 4
		assertEquals(SUCCESS,createGroup("public", "system", true,null));
		assertEquals(SUCCESS,requestRole("public", "system", "site",null));
		launchServerSideTest();

		// 5
		assertEquals(SUCCESS,leaveGroup("public", "system"));
//		assertTrue(isConnected());
		launchServerSideTest();
	
		// 6
		assertEquals(SUCCESS,createGroup("public", "system", true,null));
		assertEquals(SUCCESS,requestRole("public", "system", "site",null));
		launchServerSideTest();

		// 7
		assertEquals(SUCCESS,leaveGroup("public", "system"));
		assertNotNull(getAgentWithRole("public", "system", "site"));
//		assertTrue(isConnected());
		launchServerSideTest();
	
		pause(3000);
	}

	/**
	 * 
	 */
	private void launchServerSideTest() {
		sendMessage(other, new Message());
		pause(1000);
	}
	
}
