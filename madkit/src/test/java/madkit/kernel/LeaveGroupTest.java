package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import madkit.test.agents.CGRAgent;


/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */

public class LeaveGroupTest extends JunitMadkit {

	@Test
	public void nullArgs() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					leaveGroup(null, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					leaveGroup(COMMUNITY, null);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveGroup(null, GROUP));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				threadAssertEquals(NOT_COMMUNITY, leaveGroup(cgrDontExist(), cgrDontExist()));
			}
		});
	}

	@Test
	public void notInGroup() {
		launchTestedAgent(new GenericTestAgent() {
			protected void onActivation() {
				launchAgent(new CGRAgent());
				threadAssertTrue(getOrgnization().isCommunity(COMMUNITY));
				threadAssertTrue(getOrgnization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_IN_GROUP, leaveGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void notGroupNotCommunity() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_GROUP, leaveGroup(COMMUNITY, cgrDontExist()));
				threadAssertEquals(NOT_COMMUNITY, leaveGroup(cgrDontExist(), GROUP));
			}
		});
	}

	@Test
	public void leaveGroup() {
		launchTestedAgent(new Agent() {
			protected void onActivation() {
				assertFalse(getOrgnization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_COMMUNITY, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(getOrgnization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertFalse(getOrgnization().isCommunity(COMMUNITY));
				assertFalse(getOrgnization().isGroup(COMMUNITY, GROUP));

				// second run
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				assertTrue(getOrgnization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertFalse(getOrgnization().isCommunity(COMMUNITY));
				assertFalse(getOrgnization().isGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void killBeforeLeaveGroup() {

		launchTestedAgent(new Agent() {
			protected void onActivation() {
				assertFalse(getOrgnization().isCommunity(COMMUNITY));
				CGRAgent a = new CGRAgent();
				launchAgent(a);
				threadAssertEquals(ALREADY_GROUP, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, a.leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_IN_GROUP, a.leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, a.createGroup(COMMUNITY, GROUP));
				killAgent(a);
				assertFalse(getOrgnization().isCommunity(COMMUNITY));
			}
		});
	}

}
