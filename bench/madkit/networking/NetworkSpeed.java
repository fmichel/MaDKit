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

import java.util.logging.Level;

import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Message;
import madkit.testing.util.agent.ForEverOnTheSameAASenderAgent;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */

public class NetworkSpeed extends JunitMadkit {

	@Test
	public void networkPingPong() {
		addMadkitArgs(BooleanOption.network.toString(),LevelOption.kernelLogLevel.toString(),Level.ALL.toString(),LevelOption.networkLogLevel.toString(),Level.OFF.toString());
		launchTest(new NormalAgent() {
			@Override
			protected void activate() {
				setLogLevel(Level.OFF);
				createGroup(COMMUNITY, GROUP,true);
				requestRole(COMMUNITY, GROUP, ROLE);
				launchCustomNetworkInstance(Level.OFF, ForEverOnTheSameAASenderAgent.class);
				AgentAddress aa = waitNextMessage().getSender();
				Message m = null;
				for (int i = 0; i < 100; i++) {
					startTimer();
					sendMessage(aa, new Message());
					m = waitNextMessage();
					stopTimer("");
				}
				System.err.println(m);
			}
		});
		cleanHelperMDKs();
	}

	@Test
	public void internal() {
		final int nbOfExchanges = 100000;
		addMadkitArgs(BooleanOption.network.toString(),LevelOption.kernelLogLevel.toString(),Level.ALL.toString(),LevelOption.networkLogLevel.toString(),Level.OFF.toString());
		launchTest(new NormalAgent() {
			@Override
			protected void activate() {
				setLogLevel(Level.OFF);
				createGroup(COMMUNITY, GROUP,true);
				requestRole(COMMUNITY, GROUP, ROLE);
				ForEverOnTheSameAASenderAgent a;
				launchAgent(a = new ForEverOnTheSameAASenderAgent());
				a.setLogLevel(Level.OFF);
				AgentAddress aa = waitNextMessage().getSender();
				Message m = null;
				startTimer();
				for (int i = 0; i < nbOfExchanges; i++) {
					sendMessage(aa, new Message());
					m = waitNextMessage();
				}
				stopTimer("for "+nbOfExchanges+ " messages exchanged ");
				System.err.println(m);
			}
		});
	}

}