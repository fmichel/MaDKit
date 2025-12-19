package networking.organization;

import java.util.List;

import org.testng.annotations.Test;

import madkit.kernel.AgentAddress;
import madkit.kernel.MadkitTestInstance;
import madkit.kernel.Organization;
import madkit.test.agents.DistributedCGRAgent;
import madkit.test.utils.MadkitNetworkTestCase;

public class OrganizationSharingTest extends MadkitNetworkTestCase {
	@Test
	public void givenAgentInOrg_whenConnected_thenOtherKernelSeeThisAgent() {
		runTest(new DistributedCGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance();
				pause(1000);
				lineBreak();
				Organization foreignNetworkOrg = otherMK.getOrganization();
				List<AgentAddress> l = foreignNetworkOrg.getAgentsWithRole(COMMUNITY, GROUP, ROLE);
				getLogger().info("Agents in " + COMMUNITY + "/" + GROUP + "/" + ROLE + " from foreign org: " + l);
				threadAssertEquals(1, l.size());
				resume();
			}
		});
	}

}
