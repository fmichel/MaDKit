package networking;

import java.util.List;
import java.util.logging.Level;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import helpers.agents.DistributedReplier;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.MadkitTestInstance;
import madkit.kernel.Message;
import madkit.kernel.Organization;
import madkit.test.agents.DistributedCGRAgent;

public class MadkitTestInstanceTest extends MadkitConcurrentTestCase {

	@Override
	protected String[] getMadkitTestArgs() {
		return new String[] { "--network" };
	}

//	@Test
	public void givenAMadkitConcurrentTestCase_whenLaunchAHelpInstance_thenIsAbleToCloseAll() {
		runTest(new DistributedCGRAgent() {
			@Override
			protected void onActivation() {
				launchAgent(new Agent() {
					@Override
					protected void onLive() {
					}
				});
				super.onActivation();
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance("--agents",
						DistributedReplier.class.getName());
				AgentAddress foreignAgent = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				ReturnCode r = send(new Message(), foreignAgent);
				threadAssertEquals(SUCCESS, r);
				getLogger().setLevel(Level.ALL);
				Message m = waitNextMessage(10000);
				threadAssertNotNull(m);
				otherMK.exit();
				m = waitNextMessage(50000);
				resume();
			}
		});
	}

//	@Test
	public void givenAgentInOrg_whenConnected_thenCanSendMessageToForeignAgent() {
		runTest(new DistributedCGRAgent() {
			@Override
			protected void onActivation() {
				super.onActivation();
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance("--agents",
						DistributedReplier.class.getName());
				Organization foreignNetworkOrg = otherMK.getOrganization();
				System.err.println(foreignNetworkOrg.getOrganizationSnapShot(false));
				List<AgentAddress> l = foreignNetworkOrg.getAgentsWithRole(COMMUNITY, GROUP, ROLE);
				getLogger().info("Agents in " + COMMUNITY + "/" + GROUP + "/" + ROLE + " from foreign org: " + l);
				threadAssertEquals(2, l.size());
				AgentAddress foreignAgent = getAgentWithRole(COMMUNITY, GROUP, ROLE);
				ReturnCode r = send(new Message(), foreignAgent);
				threadAssertEquals(SUCCESS, r);
				getLogger().setLevel(Level.ALL);
				Message m = waitNextMessage(10000);
				threadAssertNotNull(m);
				MadkitTestInstance.cleanUpInstances();
				resume();
			}
		});
	}

}
