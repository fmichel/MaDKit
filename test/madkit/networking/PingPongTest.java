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

import org.junit.Test;

import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.AgentAddress;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.messages.ObjectMessage;
import madkit.testing.util.agent.PongAgent;
import static madkit.kernel.JunitMadKit.COMMUNITY;
import static madkit.kernel.JunitMadKit.GROUP;
import static madkit.kernel.JunitMadKit.ROLE;
import static org.junit.Assert.*;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class PingPongTest extends JunitMadKit
{
	@Test
	public void networkPingPong()
	{
		addMadkitArgs(BooleanOption.network.toString());
		launchTest(new Agent(){
			@Override
			protected void activate() {
//		setLogLevel(Level.OFF);
				createGroupIfAbsent(COMMUNITY,GROUP,true, null);
				requestRole(COMMUNITY,GROUP,ROLE,null);
				PongAgent.main(null);
				assertNotNull(waitNextMessage(10000));
			}
		});
	}

}