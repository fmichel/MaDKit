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

import madkit.agr.CloudCommunity;
import madkit.gui.actions.MadkitAction;
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
public class DiscoverTest extends JunitMadKit{

	@Test
	public void multipleConnectionTest() {
		addMadkitArgs(BooleanOption.network.toString());
		launchTest(new AbstractAgent(){
			@Override
			protected void activate() {
				launchMKNetworkInstance();		
				launchMKNetworkInstance();		
				launchMKNetworkInstance();	
				launchMKNetworkInstance();	
				launchMKNetworkInstance();	
				pause(2000);
				List<AgentAddress> l = getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT);
				for (AgentAddress agentAddress : l) {
					System.err.println(agentAddress);
				}
				assertEquals(6, l.size());
				MadkitAction.MADKIT_STOP_NETWORK.getAction(this).actionPerformed(null);
				pause(100);
				
				//not connected 
				assertFalse(isCommunity(CloudCommunity.NAME));

				//second round
				MadkitAction.MADKIT_LAUNCH_NETWORK.getAction(this).actionPerformed(null);
				pause(1000);
				l = getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT);
				for (AgentAddress agentAddress : l) {
					System.err.println(agentAddress);
				}
				assertEquals(6, l.size());
			}
		});
	}	
}
