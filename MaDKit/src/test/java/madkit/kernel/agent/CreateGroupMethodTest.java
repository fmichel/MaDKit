package madkit.kernel.agent;

import static madkit.kernel.Agent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.Agent.ReturnCode.SUCCESS;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import madkit.kernel.JunitMadkit;

/**
 *
 *
 */
public class CreateGroupMethodTest extends JunitMadkit {

	@Test
	public void givenEmptyOrg_whenSuccess_thenIsGroupReturnsTrue() {
		testBehavior(agent -> {
			assertEquals(agent.createGroup(COMMUNITY, GROUP), SUCCESS);
			assertTrue(agent.getOrganization().isCommunity(COMMUNITY));
			assertTrue(agent.getOrganization().isGroup(COMMUNITY, GROUP));
		});
	}

	@Test
	public void givenGroupExists_whenCreateGroup_thenALREADY_GROUPisReturned() {
		testBehavior(agent -> {
			assertEquals(agent.createGroup(COMMUNITY, GROUP), SUCCESS);
			assertEquals(agent.createGroup(COMMUNITY, GROUP), ALREADY_GROUP);
		});
	}

}
