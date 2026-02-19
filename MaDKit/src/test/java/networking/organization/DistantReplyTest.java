package networking.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.logging.Level;

import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import helpers.agents.DistributedReplier;
import madkit.kernel.AgentAddress;
import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitNetworkConcurrentTestCase;
import madkit.kernel.MadkitTestInstance;
import madkit.kernel.Message;
import madkit.kernel.Organization;

public class DistantReplyTest extends MadkitNetworkConcurrentTestCase {

	@Test
	public void givenAgentNotInOrgPriorly_whenRequestRole_thenDistantAgentCanReply() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance("--agents",
						DistributedReplier.class.getName());
				Organization foreignNetworkOrg = otherMK.getOrganization();
				System.err.println(foreignNetworkOrg.getOrganizationSnapShot(false));
				List<AgentAddress> l = foreignNetworkOrg.getAgentsWithRole(COMMUNITY, GROUP, ROLE);
				getLogger().info("Agents in " + COMMUNITY + "/" + GROUP + "/" + ROLE + " from foreign org: " + l);
				assertThat(l.size()).as("expected number of agents in foreign org").isEqualTo(1);
				requestRole(COMMUNITY, GROUP, ROLE);
				pause(1000);
				System.err.println(foreignNetworkOrg.getOrganizationSnapShot(false));
				AgentAddress foreignAgent = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				ReturnCode r = send(new Message(), foreignAgent);
				assertThat(r).as("send return code").isEqualTo(SUCCESS);
				getLogger().setLevel(Level.ALL);
				Message m = waitNextMessage(10000);
				assertThat(m).as("received message from foreign agent").isNotNull();
				resume();
			}
		});
	}

}