/*
 * Copyright 1997-2012 Fabien Michel, Olivier Gutknecht, Jacques Ferber
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

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.logging.Level;

import madkit.action.KernelAction;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.kernel.Madkit.Option;
import madkit.testing.util.agent.PongAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class SecurePingPongTest extends JunitMadkit {
	@Test
	public void networkPingPong() {
		addMadkitArgs(BooleanOption.network.toString(),LevelOption.kernelLogLevel.toString(),Level.ALL.toString(),LevelOption.networkLogLevel.toString(),Level.ALL.toString());
		launchTest(new Agent() {
			@Override
			protected void activate() {
				Madkit m = new Madkit(Option.launchAgents.toString(),Denier.class.getName(),BooleanOption.network.toString());
				pause(1000);
				assertEquals(ReturnCode.ACCESS_DENIED, requestRole(COMMUNITY, GROUP, ROLE));
//				m.doAction(KernelAction.EXIT);
//				KernelAction.STOP_NETWORK.getActionFor(this).actionPerformed(null);
//				KernelAction.EXIT.getActionFor(this).actionPerformed(null);
			}
		});
	}

}