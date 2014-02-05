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
package madkit.scenari.bug;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.FaultyAA;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.10
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class RuntimeExceptionTest extends JunitMadkit {

	@Test
	public void nullPointerInActivate() {
		AbstractAgent a;
		launchTest(a = new FaultyAA(true),ReturnCode.AGENT_CRASH,true);
		System.err.println(getKernel().getOrganizationSnapShot(false));
		assertAgentIsTerminated(a);
		assertFalse(getKernel().isCommunity(COMMUNITY));
	}

	@Test
	public void nullPointerInEnd() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				AbstractAgent a = new FaultyAA(false,true);
				assertEquals(SUCCESS, launchAgent(a));
				assertTrue(a.isAlive());
				assertTrue(getKernel().isCommunity(COMMUNITY));
				assertEquals(SUCCESS, killAgent(a));
				assertEquals(ALREADY_KILLED, killAgent(a));
				assertAgentIsTerminated(a);
				assertFalse(getKernel().isCommunity(COMMUNITY));
			}
		});
	}

}
