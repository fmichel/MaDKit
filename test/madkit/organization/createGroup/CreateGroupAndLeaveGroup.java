/**
 * 
 */
package madkit.organization.createGroup;

import static madkit.kernel.AbstractAgent.ReturnCode.*;
import static org.junit.Assert.*;

import org.junit.Test;

import madkit.kernel.Madkit;
import madkit.kernel.Madkit.Roles;
import test.util.JUnitBooterAgent;
/**
 * @author fab
 *
 */
public class CreateGroupAndLeaveGroup extends JUnitBooterAgent{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3927821429568872341L;
	static boolean distributedGroup = false;
	
	@Test
	public void madkitInit() {
	}

	
	@Test
	public void testWithoutNetworkAndNotDistributed() {
		distributedGroup = false;
		System.err.println(getBinTestDir());
		String[] args = {"--autoAgentLogFile",getBinTestDir(),"--agentLogLevel","ALL","--MadkitLogLevel","OFF","--orgLogLevel","ALL","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}

	@Test
	public void testWithNetworkAndNotDistributed() {
		distributedGroup = false;
		String[] args = {"--network","--autoAgentLogFile",getBinTestDir(),"--agentLogLevel","FINE","--MadkitLogLevel","OFF","--orgLogLevel","ALL","--launchAgents",getClass().getName()};
		Madkit.main(args);
		pause(5000);
	}

	@Test
	public void testWithoutNetworkAndDistributed() {
		distributedGroup = true;
		System.err.println(getBinTestDir());
		String[] args = {"--autoAgentLogFile",getBinTestDir(),"--agentLogLevel","OFF","--MadkitLogLevel","OFF","--orgLogLevel","ALL","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}

	@Test
	public void testWithNetworkAndDistributed() {
		distributedGroup = true;
		String[] args = {"--network","--autoAgentLogFile",getBinTestDir(),"--agentLogLevel","FINE","--MadkitLogLevel","OFF","--orgLogLevel","ALL","--launchAgents",getClass().getName()};
		Madkit.main(args);
	}

	/** 
	 * @see madkit.kernel.AbstractMadkitBooter#activate()
	 */
	@Override
	public void activate() {
		super.activate();
		testCreateGroup();
//		setMadkitProperty(MadkitCommandLine.ORG_LOG_LEVEL, "ALL");

}

	/**
	 * 
	 */
	private void testCreateGroup() {
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP,distributedGroup,null));
		assertEquals(ALREADY_GROUP, createGroup(COMMUNITY, GROUP,distributedGroup,null));
		assertEquals(false, createGroupIfAbsent(COMMUNITY, GROUP,distributedGroup,null));

		assertTrue(isGroup(COMMUNITY, GROUP));
		assertTrue(isRole(COMMUNITY, GROUP,Roles.GROUP_MANAGER_ROLE));
		assertTrue(isCommunity(COMMUNITY));
		
		assertEquals(NOT_COMMUNITY, leaveGroup(COMMUNITY+"d", GROUP));
		assertEquals(NOT_GROUP, leaveGroup(COMMUNITY, GROUP+"d"));

		
		//Manager role cannot be requested
		assertEquals(ROLE_ALREADY_HANDLED, requestRole(COMMUNITY, GROUP,Roles.GROUP_MANAGER_ROLE));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP,ROLE));

		assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
		assertFalse(isCommunity(COMMUNITY));
		assertFalse(isGroup(COMMUNITY, GROUP));
		assertFalse(isRole(COMMUNITY, GROUP,Roles.GROUP_MANAGER_ROLE));

		//Rerun to test with another agent inside the group
		
		assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP,distributedGroup,null));
		assertEquals(ROLE_ALREADY_HANDLED, testAgent.requestRole(COMMUNITY, GROUP,Roles.GROUP_MANAGER_ROLE));
		assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP,ROLE));
		assertEquals(SUCCESS, testAgent.requestRole(COMMUNITY, GROUP,ROLE));
		assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
		assertTrue(isCommunity(COMMUNITY));
		assertTrue(isGroup(COMMUNITY, GROUP));
		//a new agent should have been selected
//		assertTrue(isRole(COMMUNITY, GROUP,Madkit.GROUP_MANAGER_ROLE));
	}

}
