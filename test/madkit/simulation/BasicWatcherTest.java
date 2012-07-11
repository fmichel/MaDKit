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
package madkit.simulation;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Probe;
import madkit.kernel.Watcher;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */
@SuppressWarnings("serial")
public class BasicWatcherTest extends JunitMadkit {

	@Test
	public void addingNullProbe() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				Watcher s = new Watcher();
				assertEquals(SUCCESS, launchAgent(s));
				try {
					Probe<AbstractAgent> a = new Probe<AbstractAgent>(null, null, null);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Probe<AbstractAgent> a = new Probe<AbstractAgent>(COMMUNITY, null, null);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Probe<AbstractAgent> a = new Probe<AbstractAgent>(COMMUNITY, GROUP, null);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Probe<AbstractAgent> a = new Probe<AbstractAgent>(null, GROUP, null);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Probe<AbstractAgent> a = new Probe<AbstractAgent>(null, null, ROLE);
					s.addProbe(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void addingAndRemovingProbes() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				Watcher s = new Watcher();
				assertEquals(SUCCESS, launchAgent(s));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Probe<AbstractAgent> a = new Probe<AbstractAgent>(COMMUNITY, GROUP, ROLE);
				s.addProbe(a);
				assertEquals(1, a.size());

				code = leaveRole(COMMUNITY, GROUP, ROLE);
				assertEquals(SUCCESS, code);
				assertEquals(0, a.size());

				assertEquals(ALREADY_GROUP, createGroup(COMMUNITY, GROUP, false, null));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE, null));

				assertEquals(1, a.size());

				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertEquals(0, a.size());

				// Adding and removing while group does not exist
				s.removeProbe(a);
				assertEquals(0, a.size());
				s.addProbe(a);
				assertEquals(0, a.size());

				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, null));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE, null));
				AbstractAgent other = new AbstractAgent() {
					protected void activate() {
						assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE, null));
					}
				};
				assertEquals(SUCCESS, launchAgent(other));

				assertEquals(2, a.size());
				s.removeProbe(a);
				assertEquals(0, a.size());

				s.addProbe(a);
				assertEquals(2, a.size());

				assertEquals(SUCCESS, leaveGroup(COMMUNITY, GROUP));
				assertEquals(1, a.size());
				assertEquals(SUCCESS, other.leaveGroup(COMMUNITY, GROUP));
				assertEquals(0, a.size());

				assertEquals(SUCCESS, createGroup(COMMUNITY, GROUP, false, null));
				assertEquals(SUCCESS, requestRole(COMMUNITY, GROUP, ROLE, null));
				assertEquals(SUCCESS, other.requestRole(COMMUNITY, GROUP, ROLE, null));
				assertEquals(2, a.size());

				killAgent(s);
				assertEquals(0, a.size());
			}
		});
	}

}
