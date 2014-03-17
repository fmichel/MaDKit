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
package madkit.api.Agent;

import static org.junit.Assert.assertEquals;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NormalAgent;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.7
 * @version 0.9
 * 
 */
public class setThreadPriorityTest extends JunitMadkit {

	@Test
	public void nullCommunity() {
		launchTest(new NormalAgent() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void activate() {
				assertEquals(Thread.NORM_PRIORITY -1, getThreadPriority());
				setThreadPriority(2);
				assertEquals(2, getThreadPriority());
			}
			
			@Override
			protected void live() {
				try {
					assertEquals(2, getThreadPriority());
					setThreadPriority(3);
				} catch (AssertionError e) {
					testFails(e);
				}
			}
			
			@Override
			protected void end() {
				try {
					assertEquals(3, getThreadPriority());
				} catch (AssertionError e) {
					testFails(e);
				}
			}
		});
		pause(100);
		everythingOK();
	}
}
