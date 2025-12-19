/*******************************************************************************
 * MaDKit - Multi-agent systems Development Kit 
 * 
 * Copyright (c) 1998-2025 Fabien Michel, Olivier Gutknecht, Jacques Ferber...
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java API for developing and simulating 
 * Multi-Agent Systems (MAS) using an organizational perspective.
 *
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.You can use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty and the software's author, the holder of the
 * economic rights, and the successive licensors have only limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading, using, modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean that it is complicated to manipulate, and that also
 * therefore means that it is reserved for developers and experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and, more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 *******************************************************************************/
package networking;

import java.util.List;
import java.util.logging.Level;

import org.testng.annotations.AfterMethod;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import helpers.agents.DistributedReplier;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.MadkitConcurrentTestCase;
import madkit.kernel.MadkitTestInstance;
import madkit.kernel.Message;
import madkit.kernel.Organization;
import madkit.network.NetworkCommunity;
import madkit.test.agents.DistributedCGRAgent;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class OragnizationSharingTest extends MadkitConcurrentTestCase {

	@Override
	protected String[] getMadkitTestArgs() {
		return new String[] { "--network" };
	}

	@AfterMethod
	public void waitSomeTimeBetweenTests() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

//	@Test
	public void givenKernels_whenConnected_thenTheyShareNetworkOrganization() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance();
				List<AgentAddress> l = getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				for (AgentAddress agentAddress : l) {
					System.err.println(agentAddress);
				}
				lineBreak();
				threadAssertEquals(2, l.size());
				Organization foreignNetworkOrg = otherMK.getOrganization();
				l = foreignNetworkOrg.getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				threadAssertEquals(2, l.size());

				MadkitTestInstance anotherMK = MadkitTestInstance.getNetworkInstance();
				l = getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				threadAssertEquals(3, l.size());
				l = foreignNetworkOrg.getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				threadAssertEquals(3, l.size());

				Organization anotherNetworkOrg = anotherMK.getOrganization();
				l = anotherNetworkOrg.getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				threadAssertEquals(3, l.size());
				resume();
			}
		});
	}

//	@Test
	public void testss() {
		runTest(new Agent() {
			@Override
			protected void onActivation() {
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance("--agents",
						DistributedReplier.class.getName());
				otherMK = MadkitTestInstance.getNetworkInstance("--agents", DistributedReplier.class.getName());
				otherMK = MadkitTestInstance.getNetworkInstance("--agents", DistributedReplier.class.getName());
				resume();
			}
		});
	}

//	@Test
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
				threadAssertEquals(r, SUCCESS);
				getLogger().setLevel(Level.ALL);
				Message m = waitNextMessage(10000);
				threadAssertNotNull(m);
				resume();
			}
		});
	}

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
