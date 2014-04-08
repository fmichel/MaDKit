/*
 * Copyright 1997-2013 Fabien Michel, Olivier Gutknecht, Jacques Ferber
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * MaDKit is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.networking;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.logging.Level;

import madkit.action.KernelAction;
import madkit.agr.CloudCommunity;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */

public class ConnectionTest extends JunitMadkit {

	@Test
	public void simpleConnectionTest() {
//		pause(5000);
		addMadkitArgs(BooleanOption.network.toString()
//				,
				,LevelOption.networkLogLevel.toString(),"FINE"
//				LevelOption.kernelLogLevel.toString(),"FINER",
//				BooleanOption.createLogFiles.toString(),
//				Option.launchAgents.toString(),ForEverAgent.class.getName()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				Madkit m = launchMKNetworkInstance(Level.OFF);
				pause(200);
				List<AgentAddress> l = getAgentsWithRole(
						CloudCommunity.NAME, 
						CloudCommunity.Groups.NETWORK_AGENTS,
						CloudCommunity.Roles.NET_AGENT);
				for (AgentAddress agentAddress : l) {
					System.err.println(agentAddress);
				}
				lineBreak();
				System.err.println(getOrganizationSnapShot(true));
				lineBreak();
				assertEquals(2, l.size());
				m.doAction(KernelAction.EXIT);
				KernelAction.EXIT.getActionFor(this).actionPerformed(null);
				pause(100);
			}
		});
	}
}
