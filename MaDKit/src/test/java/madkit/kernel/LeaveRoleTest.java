package madkit.kernel;

import static madkit.kernel.Agent.ReturnCode.NOT_COMMUNITY;
import static madkit.kernel.Agent.ReturnCode.NOT_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_IN_GROUP;
import static madkit.kernel.Agent.ReturnCode.NOT_ROLE;
import static madkit.kernel.Agent.ReturnCode.ROLE_NOT_HANDLED;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import org.testng.annotations.Test;

import madkit.agr.SystemRoles;

/**
 *
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */

public class LeaveRoleTest extends JunitMadkit {

	@Test
	public void returnSuccess() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertTrue(getOrganization().isGroup(COMMUNITY, GROUP));
				threadAssertEquals(SUCCESS, leaveRole(COMMUNITY, GROUP, SystemRoles.GROUP_MANAGER));
				// leaveGroup by leaving roles
				threadAssertFalse(getOrganization().isCommunity(COMMUNITY));
				threadAssertFalse(getOrganization().isGroup(COMMUNITY, GROUP));
			}
		});
	}

	@Test
	public void returnNotCgr() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_COMMUNITY, leaveRole(cgrDontExist(), GROUP, ROLE));
				threadAssertEquals(NOT_GROUP, leaveRole(COMMUNITY, cgrDontExist(), ROLE));
				threadAssertEquals(NOT_ROLE, leaveRole(COMMUNITY, GROUP, cgrDontExist()));
				threadAssertEquals(SUCCESS, launchAgent(new Agent() {
					@Override
					protected void onActivation() {
						requestRole(COMMUNITY, GROUP, ROLE);
					}
				}));
				threadAssertEquals(ROLE_NOT_HANDLED, leaveRole(COMMUNITY, GROUP, ROLE));
				threadAssertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				threadAssertEquals(NOT_IN_GROUP, leaveRole(COMMUNITY, GROUP, ROLE));
			}
		});
	}

	@Test
	public void nullArgs() {
		launchTestedAgent(new Agent() {
			@Override
			protected void onActivation() {
				threadAssertEquals(SUCCESS, createGroup(COMMUNITY, GROUP));
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveRole(null, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_GROUP, leaveRole(COMMUNITY, null, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(ROLE_NOT_HANDLED, leaveRole(COMMUNITY, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveRole(null, GROUP, null));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveRole(null, GROUP, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					threadAssertEquals(NOT_COMMUNITY, leaveRole(null, null, ROLE));
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

}
