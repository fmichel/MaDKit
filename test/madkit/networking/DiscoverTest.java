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

import static org.junit.Assert.assertFalse;

import java.util.logging.Level;

import madkit.action.KernelAction;
import madkit.agr.CloudCommunity;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

import org.junit.After;
import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class DiscoverTest extends JunitMadkit {
	
	protected static final int	OTHERS	= 6;

	@After
	public void clean(){
		cleanHelperMDKs();
	}

	@Test
	public void multipleConnectionTest() {
		cleanHelperMDKs();
		addMadkitArgs(BooleanOption.network.toString(),LevelOption.networkLogLevel.toString(),"FINE"
				,LevelOption.madkitLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				KernelAction.LAUNCH_NETWORK.getActionFor(this).actionPerformed(null);
				for (int i = 0; i < OTHERS; i++) {
					launchMKNetworkInstance(Level.OFF);
				}
				pause(300);
				testConnections(this);
			}
		});
	}
	
	@Test
	public void multipleExternalConnectionTest() {
		cleanHelperMDKs();
		addMadkitArgs(BooleanOption.network.toString(),LevelOption.networkLogLevel.toString(),"FINE"
				,LevelOption.madkitLogLevel.toString(),Level.ALL.toString()
				);
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				KernelAction.LAUNCH_NETWORK.getActionFor(this).actionPerformed(null);
				for (int i = 0; i < OTHERS; i++) {
					launchExternalNetworkInstance();
				}
				testConnections(this);
			}
		});
	}

	/**
	 * @param agent 
	 * 
	 */
	private void testConnections(AbstractAgent agent) {
		agent.setLogLevel(Level.INFO);
		checkConnectedIntancesNb(agent, OTHERS+1);
//		List<AgentAddress> l = agent.getAgentsWithRole(CloudCommunity.NAME, CloudCommunity.Groups.NETWORK_AGENTS,
//				CloudCommunity.Roles.NET_AGENT);
//		for (AgentAddress agentAddress : l) {
//			System.err.println(agentAddress);
//		}
//		assertEquals(OTHERS+1, l.size());
		KernelAction.STOP_NETWORK.getActionFor(agent).actionPerformed(null);
		startTimer();
		do {
			pause(200);
		}
		while (stopTimer("") < 10000 && agent.isCommunity(CloudCommunity.NAME));

//		System.err.println(agent.getOrganizationSnapShot(true));
		// not connected
		assertFalse(agent.isCommunity(CloudCommunity.NAME));

		// second round
		KernelAction.LAUNCH_NETWORK.getActionFor(agent).actionPerformed(null);
		pause(300);
		checkConnectedIntancesNb(agent, OTHERS+1);
		cleanHelperMDKs();
		checkConnectedIntancesNb(agent, 1);
		KernelAction.EXIT.getActionFor(agent).actionPerformed(null);
	}

}
