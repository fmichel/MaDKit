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
import madkit.agr.CloudCommunity;
import madkit.agr.LocalCommunity;
import madkit.agr.LocalCommunity.Groups;
import madkit.gui.actions.MadkitActions;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.Option;
import madkit.testing.util.agent.ForEverAgent;

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
				if(logger != null)
					logger.info(""+getAgentsWithRole(LocalCommunity.NAME, Groups.NETWORK,LocalCommunity.Roles.NET_AGENT));
				assertEquals(6, getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT).size());
				MadkitActions.MADKIT_STOP_NETWORK.getAction(this).actionPerformed(null);
				pause(100);
				
				//not connected 
				assertFalse(isCommunity(CloudCommunity.NAME));

				//second round
				MadkitActions.MADKIT_LAUNCH_NETWORK.getAction(this).actionPerformed(null);
				pause(1000);
				assertEquals(6, getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS, CloudCommunity.Roles.NET_AGENT).size());
			}
		});
	}	
}
