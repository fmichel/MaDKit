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
package madkit.classreloading;

import static org.junit.Assert.fail;

import java.util.logging.Level;

import madkit.action.AgentAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.testing.util.agent.NormalAA;

import org.junit.Test;

/**
 * bin directory should be cleaned before use
 * the .class file is part of the test
 * 
 * @author Fabien Michel
 * @since MadKit 5.0.0.20
 * @version 0.9
 * 
 */
public class ReloadByGUITest extends JunitMadkit {

	@Test
	public void noExceptionTest() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 1L;

			protected void activate() {
				NormalAA a = new NormalAA(){
					/**
					 * 
					 */
					private static final long	serialVersionUID	= 1L;

					@Override
					protected void activate() {
						super.activate();
					}
				};
				launchAgent(a);
				try {
					setLogLevel(Level.ALL);
					AgentAction.RELOAD.getActionFor(a).actionPerformed(null);
				} catch (Throwable e) {
					fail(e.getMessage());
				}
		}
		});
	}


	@Test
	public void noExceptionOnAATest() {
		launchTest(new AbstractAgent() {
			/**
			 * 
			 */
			private static final long	serialVersionUID	= 1L;

			protected void activate() {
				try {
					AbstractAgent a = new AbstractAgent();
					launchAgent(a);
					AgentAction.RELOAD.getActionFor(a).actionPerformed(null);
				} catch (Throwable e) {
					fail(e.getMessage());
				}
			}
		});
	}

}
