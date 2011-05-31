/**
 * 
 */
package madkit.organization;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;
import test.util.JUnitBooterAgent;

/**
 * @author fab
 *
 */
public class CreateGroupTest extends JUnitBooterAgent{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4291969943552233275L;

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		
		//create then leaveRole -> should delete the group and community
		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		assertEquals(ALREADY_GROUP,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "test",null));
		assertNull(testAgent.getAgentWithRole("public", "system", "test"));
		assertEquals(SUCCESS,testAgent.leaveRole("public", "system", "test"));
		assertEquals(ROLE_NOT_HANDLED,testAgent.leaveRole("public", "system", "test"));

		//the agent should use leaveGroup or use leave the role of manager
		assertEquals(SUCCESS,testAgent.leaveRole("public", "system", Madkit.Roles.GROUP_MANAGER_ROLE));
		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));

		//now leave directly
		assertEquals(SUCCESS,testAgent.leaveGroup("public", "system"));
		assertEquals(NOT_COMMUNITY,testAgent.leaveRole("public", "system", "test"));

		AbstractAgent other = launchAgent("madkit.kernel.AbstractAgent");
		
		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "test",null));
		assertEquals(SUCCESS,other.requestRole("public", "system", "test",null));
		assertEquals(SUCCESS,testAgent.leaveRole("public", "system", "test"));
		assertEquals(ALREADY_GROUP,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,other.leaveRole("public", "system", "test"));
		//the agent should use leaveGroup or use leave the role of manager
		assertEquals(ALREADY_GROUP,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.leaveGroup("public", "system"));
		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		
		
	}

}
