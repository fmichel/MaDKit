package networking.organization;

import java.util.List;
import java.util.logging.Level;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import helpers.agents.DistributedReplier;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.MadkitTestInstance;
import madkit.kernel.Message;
import madkit.kernel.Organization;
import madkit.test.utils.MadkitNetworkTestCase;

@Test(enabled = false)
public class DistantReplyTest extends MadkitNetworkTestCase {

//	@Test
	public void givenAgentNotInOrgPriorly_whenRequestRole_thenDistantAgentCanReply() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance("--agents",
						DistributedReplier.class.getName());
				Organization foreignNetworkOrg = otherMK.getOrganization();
				System.err.println(foreignNetworkOrg.getOrganizationSnapShot(false));
				List<AgentAddress> l = foreignNetworkOrg.getAgentsWithRole(COMMUNITY, GROUP, ROLE);
				getLogger().info("Agents in " + COMMUNITY + "/" + GROUP + "/" + ROLE + " from foreign org: " + l);
				threadAssertEquals(1, l.size());
				requestRole(COMMUNITY, GROUP, ROLE);
				pause(1000);
				System.err.println(foreignNetworkOrg.getOrganizationSnapShot(false));
				AgentAddress foreignAgent = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				ReturnCode r = send(new Message(), foreignAgent);
				threadAssertEquals(r, SUCCESS);
				getLogger().setLevel(Level.ALL);
				Message m = waitNextMessage(10000);
				threadAssertNotNull(m);
				resume();
			}
		});
	}

}
