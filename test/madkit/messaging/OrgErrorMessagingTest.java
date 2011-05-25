/**
 * 
 */
package madkit.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.*;
import static test.util.JUnitBooterAgent.*;
import static madkit.messaging.OrgErrorMessagingTest.*;

import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.Madkit.Roles;
import madkit.kernel.Message;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class OrgErrorMessagingTest extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3800617069138633163L;
	final static String OTHER = "other";
	final static String UNKNOWN = "unknown";

	
	public void activate() {
		super.activate();
		sendMessageTesting();
		
		setLogLevel(Level.OFF);

		setLogLevel(Level.FINEST);
		sendMessageTesting();
		
	}


	/**
	 * 
	 */
	private void sendMessageTesting() {
		assertEquals(SUCCESS,createGroup(COMMUNITY, GROUP, false,null));
	
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, ROLE));
		
		testAgent.createGroup(COMMUNITY, OTHER);
		testAgent.requestRole(COMMUNITY, OTHER, OTHER);
		
		assertEquals(NOT_COMMUNITY,sendMessage("unknown",GROUP,ROLE,new Message()));
		assertEquals(NOT_GROUP,sendMessage(COMMUNITY,"unknown",ROLE,new Message()));
		assertEquals(NOT_ROLE,sendMessage(COMMUNITY,GROUP,"unknown",new Message()));
		assertEquals(NOT_COMMUNITY,sendMessageWithRole("unknown",GROUP,ROLE,new Message(),"any"));
		assertEquals(NOT_GROUP,sendMessageWithRole(COMMUNITY,"unknown",ROLE,new Message(),"any"));
		assertEquals(NOT_ROLE,sendMessageWithRole(COMMUNITY,GROUP,"unknown",new Message(),"any"));

		//try in the group OTHER
		assertEquals(NOT_ROLE,sendMessage(COMMUNITY,OTHER,UNKNOWN,new Message()));
		assertEquals(NOT_IN_GROUP,sendMessage(COMMUNITY,OTHER,OTHER,new Message()));
		assertEquals(NOT_IN_GROUP,sendMessageWithRole(COMMUNITY,OTHER,OTHER,new Message(),OTHER));
		
		//the candidate role should be used to send message to the manager
		assertEquals(NOT_IN_GROUP,sendMessageWithRole(COMMUNITY,OTHER,OTHER,new Message(),Roles.GROUP_CANDIDATE_ROLE));
		assertEquals(NOT_IN_GROUP,sendMessageWithRole(COMMUNITY,OTHER,Roles.GROUP_MANAGER_ROLE,new Message(),OTHER));
		assertEquals(SUCCESS,sendMessageWithRole(COMMUNITY,OTHER,Roles.GROUP_MANAGER_ROLE,new Message(),Roles.GROUP_CANDIDATE_ROLE));
		//check reception
		Message m = testAgent.nextMessage();
		assertNotNull(m);
		assertNull(testAgent.nextMessage());
		assertEquals(Roles.GROUP_CANDIDATE_ROLE,m.getSender().getRole());
		assertEquals(Roles.GROUP_MANAGER_ROLE,m.getReceiver().getRole());
		//fake agent is replying
		assertEquals(SUCCESS,testAgent.sendMessage(m.getSender(),new Message()));

		m = nextMessage();
		assertNotNull(m);
		assertEquals(Roles.GROUP_MANAGER_ROLE,m.getSender().getRole());
		assertEquals(Roles.GROUP_CANDIDATE_ROLE,m.getReceiver().getRole());
		assertEquals(SUCCESS,sendMessage(m.getSender(),new Message()));
		
		//trash fake agent mailbox
		assertNotNull(testAgent.nextMessage());
		
		//this agent is the only one there
		assertEquals(NO_RECIPIENT_FOUND,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),"any"));
		
		//this agent is now not alone
		testAgent.requestRole(COMMUNITY, GROUP, ROLE);

		//this agent has not this role
		assertEquals(ROLE_NOT_HANDLED,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),"any"));
		
		//this agent has this role
		assertEquals(SUCCESS,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),ROLE));
		//trash fake agent mailbox
		assertNotNull(testAgent.nextMessage());
		
		
		//now take some roles to test some other properties
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, "r1"));
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, "r2"));
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, "r3"));
		
		//if I send a message without saying about the role : the receiver role is selected if I have it
		assertEquals(SUCCESS,sendMessage(COMMUNITY,GROUP,ROLE,new Message()));
		//check reception
		Message m2 = testAgent.nextMessage();
		assertNotNull(m2);
		assertNull(testAgent.nextMessage());
		assertEquals(ROLE,m2.getSender().getRole());
		assertEquals(ROLE,m2.getReceiver().getRole());
		
		//if I send a message with saying the role
		assertEquals(SUCCESS,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),"r2"));
		//check reception
		Message m3 = testAgent.nextMessage();
		assertNotNull(m3);
		assertNull(testAgent.nextMessage());
		assertEquals("r2",m3.getSender().getRole());
		assertEquals(ROLE,m3.getReceiver().getRole());
		
		assertEquals(SUCCESS,leaveGroup(COMMUNITY, GROUP));
		
		//I am not in this group anymore
		assertEquals(NOT_IN_GROUP,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),"any"));
		assertEquals(NOT_IN_GROUP,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),Roles.GROUP_CANDIDATE_ROLE));
		assertEquals(NOT_IN_GROUP,sendMessage(COMMUNITY,GROUP,ROLE,new Message()));
		assertEquals(NOT_IN_GROUP,sendMessage(COMMUNITY,GROUP,ROLE,new Message()));
		//TODO what if no manager left
		assertEquals(SUCCESS,sendMessageWithRole(COMMUNITY,GROUP,Roles.GROUP_MANAGER_ROLE,new Message(),Roles.GROUP_CANDIDATE_ROLE));

		//this agent has leaved the group so m2.getSender() is invalid
		assertEquals(INVALID_AA, testAgent.sendMessage(m2.getSender(), new Message()));
		
		m3 = testAgent.nextMessage();
		assertNotNull(m3);
		assertNull(testAgent.nextMessage());

		//testAgent can reply as group manager
		assertEquals(SUCCESS, testAgent.sendMessage(m3.getSender(), new Message()));
		
		//empty mailbox
		m3 = nextMessage();
		assertNotNull(m3);
		assertEquals(Roles.GROUP_MANAGER_ROLE, m3.getSender().getRole());
		assertEquals(Roles.GROUP_CANDIDATE_ROLE, m3.getReceiver().getRole());

		assertNull(nextMessage());
		assertNull(testAgent.nextMessage());
		
//		testThreadedAgent(new ErrorAgent());
		
		assertEquals(SUCCESS,launchAgent(new ErrorAgent()));

		//cleaning up
		assertEquals(SUCCESS,testAgent.leaveGroup(COMMUNITY, GROUP));
		assertEquals(SUCCESS,testAgent.leaveGroup(COMMUNITY, OTHER));
		assertFalse(isCommunity(COMMUNITY));
		assertFalse(isGroup(COMMUNITY, GROUP));
		assertNull(testAgent.nextMessage());
		assertNull(nextMessage());
		
	}
}

class ErrorAgent extends Agent{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2046310856238605176L;

	/**
	 * @see madkit.kernel.AbstractAgent#activate()
	 */
	@Override
	protected void activate() {
		setLogLevel(Level.INFO);

		testingSendMessage();
		
		setLogLevel(Level.OFF);

		testingSendMessage();
		
	}

	/**
	 * 
	 */
	private void testingSendMessage() {
		assertEquals(NOT_COMMUNITY,sendMessage("unknown",GROUP,ROLE,new Message()));
		assertEquals(NOT_GROUP,sendMessage(COMMUNITY,"unknown",ROLE,new Message()));
		assertEquals(NOT_ROLE,sendMessage(COMMUNITY,GROUP,"unknown",new Message()));

		assertEquals(NOT_COMMUNITY,sendMessageWithRole("unknown",GROUP,ROLE,new Message(),"any"));
		assertEquals(NOT_GROUP,sendMessageWithRole(COMMUNITY,"unknown",ROLE,new Message(),"any"));
		assertEquals(NOT_ROLE,sendMessageWithRole(COMMUNITY,GROUP,"unknown",new Message(),"any"));
		
		assertNull(sendMessageAndWaitForReply("unknown",GROUP,ROLE,new Message()));
		assertNull(sendMessageAndWaitForReply(COMMUNITY,"unknown",ROLE,new Message()));
		assertNull(sendMessageAndWaitForReply(COMMUNITY,GROUP,"unknown",new Message()));

		assertNull(sendMessageWithRoleAndWaitForReply("unknown",GROUP,ROLE,new Message(),"any"));
		assertNull(sendMessageWithRoleAndWaitForReply(COMMUNITY,"unknown",ROLE,new Message(),"any"));
		assertNull(sendMessageWithRoleAndWaitForReply(COMMUNITY,GROUP,"unknown",new Message(),"any"));
		
		
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, ROLE));

		//try in the group OTHER
		assertEquals(NOT_ROLE,sendMessage(COMMUNITY,OTHER,UNKNOWN,new Message()));
		assertEquals(NOT_IN_GROUP,sendMessage(COMMUNITY,OTHER,OTHER,new Message()));
		assertEquals(NOT_IN_GROUP,sendMessageWithRole(COMMUNITY,OTHER,OTHER,new Message(),OTHER));
		
		//the candidate role should be used to send message to the manager
		assertEquals(NOT_IN_GROUP,sendMessageWithRole(COMMUNITY,OTHER,OTHER,new Message(),Roles.GROUP_CANDIDATE_ROLE));
		assertEquals(NOT_IN_GROUP,sendMessageWithRole(COMMUNITY,OTHER,Roles.GROUP_MANAGER_ROLE,new Message(),OTHER));
		assertEquals(SUCCESS,sendMessageWithRole(COMMUNITY,OTHER,Roles.GROUP_MANAGER_ROLE,new Message(),Roles.GROUP_CANDIDATE_ROLE));
		//check reception
		Message m = testAgent.nextMessage();
		assertNotNull(m);
		assertNull(testAgent.nextMessage());
		assertEquals(Roles.GROUP_CANDIDATE_ROLE,m.getSender().getRole());
		assertEquals(Roles.GROUP_MANAGER_ROLE,m.getReceiver().getRole());
		//fake agent is replying
		assertEquals(SUCCESS,testAgent.sendMessage(m.getSender(),new Message()));

		m = nextMessage();
		assertNotNull(m);
		assertEquals(Roles.GROUP_MANAGER_ROLE,m.getSender().getRole());
		assertEquals(Roles.GROUP_CANDIDATE_ROLE,m.getReceiver().getRole());
		assertEquals(SUCCESS,sendMessage(m.getSender(),new Message()));
		
		//trash fake agent mailbox
		assertNotNull(testAgent.nextMessage());
		
		//this agent is the only one there
		assertEquals(ROLE_NOT_HANDLED,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),"any"));
		
		//this agent is now not alone
		testAgent.requestRole(COMMUNITY, GROUP, ROLE);

		//this agent has not this role
		assertEquals(ROLE_NOT_HANDLED,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),"any"));
		
		//this agent has this role
		assertEquals(SUCCESS,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),ROLE));
		//trash fake agent mailbox
		assertNotNull(testAgent.nextMessage());
		
		
		//now take some roles to test some other properties
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, "r1"));
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, "r2"));
		assertEquals(SUCCESS,requestRole(COMMUNITY, GROUP, "r3"));
		
		//if I send a message without saying about the role : the receiver role is selected if I have it
		assertEquals(SUCCESS,sendMessage(COMMUNITY,GROUP,ROLE,new Message()));
		//check reception
		Message m2 = testAgent.nextMessage();
		assertNotNull(m2);
		assertNull(testAgent.nextMessage());
		assertEquals(ROLE,m2.getSender().getRole());
		assertEquals(ROLE,m2.getReceiver().getRole());
		
		//if I send a message with saying the role
		assertEquals(SUCCESS,sendMessageWithRole(COMMUNITY,GROUP,ROLE,new Message(),"r2"));
		//check reception
		Message m3 = testAgent.nextMessage();
		assertNotNull(m3);
		assertNull(testAgent.nextMessage());
		assertEquals("r2",m3.getSender().getRole());
		assertEquals(ROLE,m3.getReceiver().getRole());

		assertEquals(SUCCESS,leaveGroup(COMMUNITY, GROUP));
	}
}