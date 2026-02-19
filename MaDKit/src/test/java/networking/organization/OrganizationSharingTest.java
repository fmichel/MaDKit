package networking.organization;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.testng.annotations.Test;

import madkit.kernel.AgentAddress;
import madkit.kernel.MadkitNetworkConcurrentTestCase;
import madkit.kernel.MadkitTestInstance;
import madkit.kernel.Organization;
import madkit.test.agents.DistributedCGRAgent;

public class OrganizationSharingTest extends MadkitNetworkConcurrentTestCase {
	@Test
	public void givenAgentInOrg_whenConnected_thenOtherKernelSeeThisAgent() {
		runTest(new DistributedCGRAgent() {
			@Override
			public void behaviorInActivate() {
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance();
				pause(1000);
				lineBreak();
				Organization foreignNetworkOrg = otherMK.getOrganization();
				List<AgentAddress> l = foreignNetworkOrg.getAgentsWithRole(COMMUNITY, GROUP, ROLE);
				getLogger().info("Agents in " + COMMUNITY + "/" + GROUP + "/" + ROLE + " from foreign org: " + l);
				assertThat(l.size()).as("expected number of agents in foreign org").isEqualTo(1);
				resume();
			}
		});
	}

}