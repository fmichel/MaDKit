/**
 * 
 */
package madkit.api.abstractAgent;

import static madkit.kernel.AbstractAgent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.AbstractAgent.ReturnCode.NOT_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static madkit.kernel.Madkit.Roles.GROUP_MANAGER_ROLE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;

import org.junit.Test;

/**
 * @author fab
 *
 */
@SuppressWarnings("serial")
public class LeaveRoleTest  extends JunitMadKit{

	@Test
	public void returnSuccess(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, requestRole(COMMUNITY,GROUP,ROLE));
				assertEquals(SUCCESS, leaveRole(COMMUNITY,GROUP,ROLE));
				assertTrue(isGroup(COMMUNITY,GROUP));
				assertEquals(SUCCESS, leaveRole(COMMUNITY,GROUP,GROUP_MANAGER_ROLE));
				//leaveGroup by leaving roles
				assertFalse(isCommunity(COMMUNITY));
				assertFalse(isGroup(COMMUNITY,GROUP));
			}});
	}

	@Test
	public void returnNotCgr(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(NOT_COMMUNITY, leaveRole(aa(),GROUP,ROLE));
				assertEquals(NOT_GROUP, leaveRole(COMMUNITY,aa(),ROLE));
				assertEquals(ROLE_NOT_HANDLED, leaveRole(COMMUNITY,GROUP,aa()));
			}});
	}

	@Test
	public void nullArgs(){
		launchTest(new AbstractAgent(){
			protected void activate() {
				assertEquals(SUCCESS, createGroup(COMMUNITY,GROUP));
				assertEquals(NOT_COMMUNITY, leaveRole(null,null,null));
				assertEquals(NOT_GROUP, leaveRole(COMMUNITY,null,null));
				assertEquals(ROLE_NOT_HANDLED, leaveRole(COMMUNITY,GROUP,null));
				assertEquals(NOT_COMMUNITY, leaveRole(null,GROUP,null));
				assertEquals(NOT_COMMUNITY, leaveRole(null,GROUP,ROLE));
				assertEquals(NOT_COMMUNITY, leaveRole(null,null,ROLE));
			}
		});
	}

}
