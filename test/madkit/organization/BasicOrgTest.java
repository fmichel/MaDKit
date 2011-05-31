/**
 * 
 */
package madkit.organization;

import static madkit.kernel.AbstractAgent.ReturnCode.ACCESS_DENIED;
import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_ALREADY_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import madkit.kernel.GroupIdentifier;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class BasicOrgTest extends JUnitBooterAgent{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 816652050438175371L;

	/* (non-Javadoc)
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		ReturnCode code;
//		setMadkitProperty(MadkitCommandLine.ORG_LOG_LEVEL, "ALL");
		/////////////////////////// REQUEST ROLE ////////////////////////
		code = testAgent.requestRole("public", "system", "test",null);
		assertEquals(NOT_COMMUNITY,code);
		code = testAgent.createGroup("public", "system", false ,null);
		assertEquals(SUCCESS,code);
		code = testAgent.requestRole("public", "syste", "site",null);
		assertEquals(NOT_GROUP,code);
		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);
		code = testAgent.requestRole("publi", "system", "site",null);
		assertEquals(NOT_COMMUNITY,code);
		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(ROLE_ALREADY_HANDLED,code);
		code = testAgent.createGroup("public", "secure", false, new GroupIdentifier() {
			@Override
			public boolean allowAgentToTakeRole(String roleName, Object memberCard) {
				return false;
			}
		});
		assertEquals(SUCCESS,code);
		code = testAgent.requestRole("public", "secure", "test",null);
		assertEquals(ACCESS_DENIED,code);
		
		
		/////////////////////////// LEAVE ROLE ////////////////////////
		
		code = testAgent.leaveRole("publicc", "system", "site");
		assertEquals(NOT_COMMUNITY,code);
		
		code = testAgent.leaveRole("public", "systemm", "site");
		assertEquals(NOT_GROUP,code);
		
		code = testAgent.leaveRole("public", "system", "sitee");
		assertEquals(ROLE_NOT_HANDLED,code);
		
		code = testAgent.leaveRole("public", "system", "site");
		assertEquals(SUCCESS,code);
		
		//it is still the group manager
		code = testAgent.leaveRole("public", "system", "site");
		assertEquals(ROLE_NOT_HANDLED,code);
		
		code = testAgent.leaveGroup("public", "system");
		assertEquals(SUCCESS,code);
		
		code = testAgent.leaveGroup("public", "secure");
		assertEquals(SUCCESS,code);
		
		/////////////////////////// LEAVE GROUP ////////////////////////
		
		code = testAgent.createGroup("public", "system", false,null);
		assertEquals(SUCCESS,code);

		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);
		
		code = testAgent.requestRole("public", "system", "site2",null);
		assertEquals(SUCCESS,code);
		
		code = testAgent.leaveGroup("public", "systemm");
		assertEquals(NOT_GROUP,code);
		
		code = testAgent.leaveGroup("publicc", "system");
		assertEquals(NOT_COMMUNITY,code);
		
		code = testAgent.leaveGroup("public", "system");
		assertEquals(SUCCESS,code);
		
		code = testAgent.requestRole("public", "system","site",null);
		assertEquals(NOT_COMMUNITY,code);
		
		code = testAgent.leaveGroup("public", "system");
		assertEquals(NOT_COMMUNITY,code);
		
		//create then leaveRole -> should delete the group
		assertEquals(SUCCESS,testAgent.createGroup("public", "system", false,null));
		assertEquals(SUCCESS,testAgent.requestRole("public", "system", "test",null));
		assertEquals(SUCCESS,testAgent.leaveRole("public", "system", "test"));
		assertNull(testAgent.getAgentWithRole("public", "system", "test"));
		assertEquals(ALREADY_GROUP,testAgent.createGroup("public", "system", false,null));

		
		/////////////////////////// AUTOMATIC LEAVE ////////////////////////////////
		code = testAgent.createGroup("public", "system", false,null);
		assertEquals(ALREADY_GROUP,code);
		code = testAgent.requestRole("public", "syste", "site",null);
		assertEquals(NOT_GROUP,code);
		code = testAgent.requestRole("public", "system", "site",null);
		assertEquals(SUCCESS,code);
		
		code = testAgent.requestRole("public", "system", "site2",null);
		assertEquals(SUCCESS,code);
		
		killAgent(testAgent);


		code = requestRole("public", "system", "test",null);
		assertEquals(NOT_COMMUNITY,code);
		
		
	}

}
