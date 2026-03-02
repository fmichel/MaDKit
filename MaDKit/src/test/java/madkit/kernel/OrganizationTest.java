package madkit.kernel;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import madkit.testing.agents.CGRAgent;

public class OrganizationTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgentWithRoleInOrg_whenGetAgentAddressOfAgentAt_thenReturnsAddress() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				AgentAddress address = getOrganization().getAddressOfAgentAt(this, COMMUNITY, GROUP, ROLE);
				assertThat(address).as("getAddressOfAgentAt should return a non-null address for an agent with a role")
						.isNotNull();
				resume();
			}
		});
	}

	@Test
	public void givenAgentWithNoRole_whenGetAgentAddressOfAgentAt_thenReturnsNull() {
		runTest(new DefaultTestAgent() {
			@Override
			public void behaviorInActivate() {
				AgentAddress address = getOrganization().getAddressOfAgentAt(this, COMMUNITY, GROUP, ROLE);
				assertThat(address).as("getAddressOfAgentAt should return null for an agent with no role").isNull();
				resume();
			}
		});
	}

}