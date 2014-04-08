/*
 * Copyright 2012 Fabien Michel
 * 
 * This file is part of MaDKit.
 * 
 * MaDKit is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * MaDKit is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with MaDKit. If not, see <http://www.gnu.org/licenses/>.
 */
package madkit.testing.util.agent;

import static madkit.kernel.JunitMadkit.COMMUNITY;
import static madkit.kernel.JunitMadkit.GROUP;
import static madkit.kernel.JunitMadkit.ROLE;

import java.util.Arrays;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.message.StringMessage;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.18
 * @version 0.9
 * 
 */
public class PongAgent extends Agent {

	protected void activate() {
		System.err.println(getOrganizationSnapShot(false));
		createGroupIfAbsent(JunitMadkit.COMMUNITY, GROUP, true, null);
		requestRole(COMMUNITY, GROUP, ROLE, null);
	}

	@Override
	public void live() {
		while (true) {
			pause(500);
			sendMessage(COMMUNITY, GROUP, ROLE, new StringMessage("test"));
			if (logger != null)
				logger.talk("\nreceived: " + nextMessage());
		}
	}

	@Override
	protected void end() {
		if (logger != null)
			logger.info("bye");
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		if (args == null) {
			args = Arrays.asList("--network", "--agentLogLevel", "ALL", "--launchAgents", PongAgent.class.getName(), ",true")
					.toArray(new String[0]);
		}
		Madkit.main(args);
	}

}
