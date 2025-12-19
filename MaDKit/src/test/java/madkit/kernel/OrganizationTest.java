package madkit.kernel;

import org.testng.annotations.Test;

import madkit.test.agents.CGRAgent;

public class OrganizationTest extends MadkitConcurrentTestCase {

	@Test
	public void givenAgentWithRoleInOrg_whenGetAgentAddressOfAgentAt_thenReturnsAddress() {
		runTest(new CGRAgent() {
			@Override
			public void behaviorInActivate() {
				AgentAddress address = getOrganization().getAddressOfAgentAt(this, COMMUNITY, GROUP, ROLE);
				threadAssertNotNull(address);
				resume();
			}
		});
	}

	@Test
	public void givenAgentWithNoRole_whenGetAgentAddressOfAgentAt_thenReturnsNull() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				AgentAddress address = getOrganization().getAddressOfAgentAt(this, COMMUNITY, GROUP, ROLE);
				threadAssertNull(address);
				resume();
			}
		});
	}

//	@DataProvider(name = "includeDistantMatrix")
//	public Object[][] includeDistantMatrix() {
//		// matrix of inputs: includeDistant = false and true
//		return new Object[][] { { false }, { true } };
//	}
//
//	@Test(dataProvider = "includeDistantMatrix")
//	public void givenOrganizationWithLocalAndDistant_whenGetOrganizationSnapShot_thenContainsExpectedEntries(
//			boolean includeDistant) {
//		// Given: an organization that has
//		// - a local group/role (always present)
//		// - an imported/distant group/role (present only when includeDistant == true)
//		// TODO: adapt the setup below to the real Organization API to create groups/roles and
//		// import a distant organization.
//		Organization org = createEmptyOrganization();
//
//		// create a local entry
//		String localCommunity = "localCommunity";
//		String localGroup = "localGroup";
//		String localRole = "localRole";
//		addLocalRole(org, localCommunity, localGroup, localRole);
//
//		// create an imported/distant entry
//		String distantCommunity = "distantCommunity";
//		String distantGroup = "distantGroup";
//		String distantRole = "distantRole";
//		addDistantRole(org, distantCommunity, distantGroup, distantRole);
//
//		// When: taking the snapshot with the tested flag
//		OrganizationSnapshot snapshot = org.getOrganizationSnapShot(includeDistant);
//
//		// Then: snapshot always contains the local entry
//		assertThat(snapshot).isNotNull();
//		assertThat(snapshot.containsKey(localCommunity)).isTrue();
//		assertThat(snapshot.get(localCommunity)).containsKey(localGroup);
//		assertThat(snapshot.get(localCommunity).get(localGroup)).containsKey(localRole);
//
//		// Then: distant entry presence depends on includeDistant
//		boolean hasDistant = snapshot.containsKey(distantCommunity)
//				&& snapshot.get(distantCommunity).containsKey(distantGroup)
//				&& snapshot.get(distantCommunity).get(distantGroup).containsKey(distantRole);
//		if (includeDistant) {
//			assertThat(hasDistant).isTrue();
//		} else {
//			assertThat(hasDistant).isFalse();
//		}
//	}

	private Organization createEmptyOrganization() {
		return new Organization(null);
	}

//	private void addLocalRole(Organization org, String community, String group, String role) {
//		 org.createGroup(community, group, Group.Role.LOCAL);
//		 org.createRole(community, group, role);
//		throw new UnsupportedOperationException("Adapt addLocalRole(...) to your API");
//	}
//
//	private void addDistantRole(Organization org, String community, String group, String role) {
//		throw new UnsupportedOperationException("Adapt addDistantRole(...) to your API");
//	}
}