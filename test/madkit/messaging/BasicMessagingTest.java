/**
 * 
 */
package madkit.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.Message;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class BasicMessagingTest extends JUnitBooterAgent{

	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7779039887981930633L;
	AbstractAgent other = new AbstractAgent();
	AbstractAgent other2 = new AbstractAgent();
	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		ReturnCode code;
		launchAgent(other);
		launchAgent(other2);
		/////////////////////////// REQUEST ROLE ////////////////////////

		code = testAgent.createGroup("public", "system", false,null);
		assertEquals(SUCCESS,code);
		code = testAgent.requestRole("public", "system", "site");
		assertEquals(SUCCESS,code);
		code = other.requestRole("public", "system", "other");
		assertEquals(SUCCESS,code);
		code = other2.requestRole("public", "system", "other");
		assertEquals(SUCCESS,code);
		
		//////////////////////// REQUESTS ///////////////////
		AgentAddress aa = other.getAgentWithRole("public", "system", "site");
		assertNotNull(aa);

		assertEquals(NULL_AA,other.sendMessage(null, new Message()));
		assertEquals(NULL_MSG,other.sendMessage(aa, null));
		
		code = other.sendMessage(aa, new Message());
		assertEquals(SUCCESS,code);

		
		Message m = testAgent.nextMessage();
		assertNotNull(m);
		if(testAgent.getLogger() != null)
			testAgent.getLogger().info("receive : "+m);

	
		code = testAgent.leaveRole("public", "system", "site");
		assertEquals(SUCCESS,code);
		
		//aa no longer exists
		code = other.sendMessage(aa, new Message());
		assertEquals(INVALID_AA,code);

		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);

		code = testAgent.sendMessage("public", "system", "site", new Message());
		assertEquals(NO_RECIPIENT_FOUND,code);

		code = testAgent.sendMessage("publicc", "system", "site", new Message());
		assertEquals(NOT_COMMUNITY,code);

		code = testAgent.sendMessage("public", "systemm", "site", new Message());
		assertEquals(NOT_GROUP,code);

		code = testAgent.sendMessage("public", "system", "sitee", new Message());
		assertEquals(NOT_ROLE,code);

		code = testAgent.sendMessage("public", "system", "site", new Message());
		assertEquals(NO_RECIPIENT_FOUND,code);

		code = testAgent.broadcastMessage("public", "system", "other", new Message());
		assertEquals(SUCCESS,code);

		// testing reception
		m = other.nextMessage();
		assertNotNull(m);
		m = other.nextMessage();
		assertNull(m);
		m = other2.nextMessage();
		assertNotNull(m);
		m = other2.nextMessage();
		assertNull(m);
		
		//testing warning
		code = testAgent.broadcastMessage("public", "system", "site", new Message());
		assertEquals(NO_RECIPIENT_FOUND,code);

		code = testAgent.broadcastMessage("publicc", "system", "other", new Message());
		assertEquals(NOT_COMMUNITY,code);

		code = testAgent.broadcastMessage("public", "systemm", "other", new Message());
		assertEquals(NOT_GROUP,code);

		code = testAgent.broadcastMessage("public", "system", "sitee", new Message());
		assertEquals(NOT_ROLE,code);

	}

}
