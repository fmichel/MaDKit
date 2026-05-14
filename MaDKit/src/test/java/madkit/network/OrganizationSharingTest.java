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
package madkit.network;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.logging.Level;

import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static madkit.kernel.Agent.ReturnCode.SUCCESS;

import helpers.agents.DistributedReplier;
import madkit.kernel.AgentAddress;
import madkit.kernel.DefaultTestAgent;
import madkit.kernel.MadkitTestInstance;
import madkit.kernel.Message;
import madkit.kernel.Organization;
import madkit.testing.agents.DistributedCGRAgent;

/**
 * @author Fabien Michel
 * @version 6.0.5
 * 
 */

public class OrganizationSharingTest extends MadkitNetworkConcurrentTestCase {

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

	@BeforeClass
	public void skipAll() {
		throw new SkipException("Network tests are disabled");
	}

	@Test
	public void givenKernels_whenConnected_thenTheyShareNetworkOrganization() {
		runTest(new DistributedCGRAgent() {
			@Override
			public void behaviorInActivate() {
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance();
				List<AgentAddress> l = getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				for (AgentAddress agentAddress : l) {
					System.err.println(agentAddress);
				}
				lineBreak();
				assertThat(l.size()).as("expected number of network agents locally").isEqualTo(2);
				Organization foreignNetworkOrg = otherMK.getOrganization();
				l = foreignNetworkOrg.getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				assertThat(l.size()).as("expected number of network agents in foreign org").isEqualTo(2);

				MadkitTestInstance anotherMK = MadkitTestInstance.getNetworkInstance();
				l = getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				assertThat(l.size()).as("expected number of network agents locally after another instance").isEqualTo(3);
				l = foreignNetworkOrg.getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				assertThat(l.size()).as("expected number of network agents in foreign org after another instance")
						.isEqualTo(3);

				Organization anotherNetworkOrg = anotherMK.getOrganization();
				l = anotherNetworkOrg.getAgentsWithRole(NetworkCommunity.NAME, NetworkCommunity.Groups.NETWORK_AGENTS,
						NetworkCommunity.Roles.NET_AGENT);
				assertThat(l.size()).as("expected number of network agents in another network org").isEqualTo(3);
				resume();
			}
		});
	}

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

	@Test
	public void givenAgentInOrg_whenConnected_thenCanSendMessageToForeignAgent() {
		runTest(new DistributedCGRAgent() {
			@Override
			public void behaviorInActivate() {
				MadkitTestInstance otherMK = MadkitTestInstance.getNetworkInstance("--agents",
						DistributedReplier.class.getName());
				Organization foreignNetworkOrg = otherMK.getOrganization();
				System.err.println(foreignNetworkOrg.getOrganizationSnapShot(false));
				List<AgentAddress> l = foreignNetworkOrg.getAgentsWithRole(COMMUNITY, GROUP, ROLE);
				getLogger().info("Agents in " + COMMUNITY + "/" + GROUP + "/" + ROLE + " from foreign org: " + l);
				assertThat(l.size()).as("expected number of agents in foreign org").isEqualTo(2);
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
