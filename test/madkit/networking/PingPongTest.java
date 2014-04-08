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

import static org.junit.Assert.assertNotNull;
import madkit.action.KernelAction;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.testing.util.agent.NormalAgent;
import madkit.testing.util.agent.PongAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */

public class PingPongTest extends JunitMadkit {
	@Test
	public void networkPingPong() {
		addMadkitArgs(BooleanOption.network.toString());
		launchTest(new NormalAgent() {
			@Override
			protected void activate() {
				// setLogLevel(Level.OFF);
				createGroupIfAbsent(COMMUNITY, GROUP, true, null);
				requestRole(COMMUNITY, GROUP, ROLE, null);
				Madkit m = new Madkit("--network", "--agentLogLevel", "ALL", "--launchAgents", PongAgent.class.getName(), ",true");
				assertNotNull(waitNextMessage(10000));
				m.doAction(KernelAction.EXIT);
				KernelAction.STOP_NETWORK.getActionFor(this).actionPerformed(null);
				KernelAction.EXIT.getActionFor(this).actionPerformed(null);
			}
		});
	}

}