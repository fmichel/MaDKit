/**
 * 
 */
package madkit.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.Roles;
import madkit.kernel.Message;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class OrganizationMessagingTest extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8611772437437729462L;
	AbstractAgent other = new AbstractAgent();
	AbstractAgent other2 = new AbstractAgent();

	@Override
	public void activate() {
		super.activate();
		ReturnCode code;
		launchAgent(other);
		launchAgent(other2);
		///////////////////////////  ////////////////////////

		createTestGroupSuccess();
		requestRoleTestSuccess();
		
		//other is not in the group
		assertEquals(NOT_IN_GROUP,other.sendMessage(COMMUNITY,GROUP,ROLE,new Message()));
		assertTrue(isRole(COMMUNITY, GROUP, Roles.GROUP_MANAGER_ROLE));
		
		//but it could send a message to the manager of that group
		assertEquals(SUCCESS,other.sendMessage(COMMUNITY,GROUP,Roles.GROUP_MANAGER_ROLE,new Message()));
		
		//but not with a role which is not candidate, so that result is still not in group
		assertEquals(NOT_IN_GROUP,other.sendMessageWithRole(COMMUNITY,GROUP,Roles.GROUP_MANAGER_ROLE,new Message(),"test"));
//		assertEquals(SUCCESS,other.requestRole(COMMUNITY,GROUP,ROLE));
//		
////		assertEquals(,other.sendMessage(COMMUNITY,GROUP,ROLE,new Message()));
//		
//		assertEquals(SUCCESS,sendMessage(COMMUNITY,GROUP,ROLE,new Message()));
//		Message reception = other.nextMessage();
//		assertEquals(reception.getSender().getRole(),ROLE);
		
	}

	
	@Test
	public void madkitInit() {
		System.err.println(getBinTestDir());
		String[] args = {"--defaultWarningLogLevel","INFO","--autoAgentLogFile",getBinTestDir(),"--agentLogLevel","INFO","--MadkitLogLevel","ALL","--orgLogLevel","ALL","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}
	

}
