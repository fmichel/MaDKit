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

import java.util.List;

import madkit.action.KernelAction;
import madkit.agr.CloudCommunity;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit.BooleanOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class AsynchronousDiscoverTest extends JunitMadKit {

	@Test
	public void multipleAsynchroneConnectionTest() {
		addMadkitArgs(BooleanOption.network.toString());
		// addMadkitArgs(LevelOption.networkLogLevel.toString(),"ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchThreadedMKNetworkInstance();
				launchThreadedMKNetworkInstance();
				launchThreadedMKNetworkInstance();
				launchThreadedMKNetworkInstance();
				launchThreadedMKNetworkInstance();
				int i = 0;
				while (getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT)
						.size() != 6) {
					pause(2000);
					if (i++ == 100)
						break;
				}
				if (logger != null)
					logger.info("" + getAgentsWithRole(LocalCommunity.NAME, Groups.NETWORK, LocalCommunity.Roles.NET_AGENT));
				assertEquals(6,
						getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT)
								.size());
				KernelAction.STOP_NETWORK.getActionFor(this).actionPerformed(null);
				pause(1000);

				// not connected
				assertFalse(isCommunity(CloudCommunity.NAME));

				// second round
				KernelAction.LAUNCH_NETWORK.getActionFor(this).actionPerformed(null);
				List<AgentAddress> l = getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS,
						CloudCommunity.Roles.NET_AGENT);
				while (l == null || l.size() != 6) {
					pause(2000);
					l = getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT);
					if (i++ == 100)
						break;
				}
				for (AgentAddress agentAddress : l) {
					System.err.println(agentAddress);
				}
				assertEquals(6, l.size());
			}
		});
	}

}
