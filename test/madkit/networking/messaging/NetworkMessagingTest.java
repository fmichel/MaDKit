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
package madkit.networking.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import madkit.kernel.JunitMadkit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.6
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class NetworkMessagingTest extends JunitMadkit {

	@Test
	public void ping() {
		addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL"
				,LevelOption.networkLogLevel.toString(), "FINE"
				);
		launchTest(new NormalAgent() {
			protected void activate() {
				setLogLevel(Level.FINE);
				assertTrue(isKernelOnline());
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, true));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				String[] args = { "--network", "--launchAgents", NetworkMessageAgent.class.getName()
//						,LevelOption.networkLogLevel.toString(),Level.FINE.toString(),
//						LevelOption.kernelLogLevel.toString(), "ALL" 
						};
				Madkit.main(args);
				assertNotNull(waitNextMessage());
			}
		});
	}
}