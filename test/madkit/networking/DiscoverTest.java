/*
 * Copyright 1997-2011 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MadKit.
 * 
 * MadKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MadKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with MadKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.networking;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.ArrayList;
import java.util.List;

import madkit.action.KernelAction;
import madkit.agr.CloudCommunity;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class DiscoverTest extends JunitMadKit {

	@Test
	public void multipleConnectionTest() {
//		addMadkitArgs(BooleanOption.network.toString(),LevelOption.networkLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				ArrayList<Madkit> mdks = new ArrayList<Madkit>();
				KernelAction.LAUNCH_NETWORK.getActionFor(this).actionPerformed(null);
				mdks.add(launchMKNetworkInstance());
				mdks.add(launchMKNetworkInstance());
				mdks.add(launchMKNetworkInstance());
				mdks.add(launchMKNetworkInstance());
				mdks.add(launchMKNetworkInstance());
				pause(300);
				List<AgentAddress> l = getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS,
						CloudCommunity.Roles.NET_AGENT);
				for (AgentAddress agentAddress : l) {
					System.err.println(agentAddress);
				}
				assertEquals(6, l.size());
				KernelAction.STOP_NETWORK.getActionFor(this).actionPerformed(null);
				pause(100);

				// not connected
				assertFalse(isCommunity(CloudCommunity.NAME));

				// second round
				KernelAction.LAUNCH_NETWORK.getActionFor(this).actionPerformed(null);
				pause(300);
				l = getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT);
				for (AgentAddress agentAddress : l) {
					System.err.println(agentAddress);
				}
				assertEquals(6, l.size());
				for (Madkit madkit : mdks) {
					madkit.doAction(KernelAction.EXIT);
				}
				KernelAction.EXIT.getActionFor(this).actionPerformed(null);
				pause(1000);
			}
		});
	}
}
