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
package madkit.networking.messaging;

import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.logging.Level;

import madkit.kernel.Agent;
import madkit.kernel.JunitMadKit;
import madkit.kernel.Madkit;
import madkit.kernel.Madkit.BooleanOption;
import madkit.kernel.Madkit.LevelOption;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MadKit 5.0.0.6
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class NetworkMessagingTest extends JunitMadKit {

	@Test
	public void ping() {
		addMadkitArgs(BooleanOption.network.toString(), LevelOption.kernelLogLevel.toString(), "ALL",
				LevelOption.networkLogLevel.toString(), "FINE");
		launchTest(new Agent() {
			protected void activate() {
				setLogLevel(Level.FINE);
				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, true));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE));
				String[] args = { "--network", "--launchAgents", NetworkMessageAgent.class.getName(),
						LevelOption.kernelLogLevel.toString(), "ALL" };
				Madkit.main(args);
				assertNotNull(waitNextMessage());
			}
		});
	}
}