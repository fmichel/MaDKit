/*
 * Copyright or © or Copr. Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)

fmichel@lirmm.fr
olg@no-distance.net
ferber@lirmm.fr

This software is a computer program whose purpose is to 
provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).

This software is governed by the CeCILL-C license under French law and
abiding by the rules of distribution of free software.  You can  use, 
modify and/ or redistribute the software under the terms of the CeCILL-C
license as circulated by CEA, CNRS and INRIA at the following URL
"http://www.cecill.info". 

As a counterpart to the access to the source code and  rights to copy,
modify and redistribute granted by the license, users are provided only
with a limited warranty  and the software's author,  the holder of the
economic rights,  and the successive licensors  have only  limited
liability. 

In this respect, the user's attention is drawn to the risks associated
with loading,  using,  modifying and/or developing or reproducing the
software by the user in light of its specific status of free software,
that may mean  that it is complicated to manipulate,  and  that  also
therefore means  that it is reserved for developers  and  experienced
professionals having in-depth computer knowledge. Users are therefore
encouraged to load and test the software's suitability as regards their
requirements in conditions enabling the security of their systems and/or 
data to be ensured and,  more generally, to use and operate it in the 
same conditions as regards security. 

The fact that you are presently reading this means that you have had
knowledge of the CeCILL-C license and that you accept its terms.
 */
package madkit.simulation;

import static madkit.kernel.AbstractAgent.ReturnCode.ALREADY_GROUP;
import static madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static org.junit.Assert.assertEquals;
import madkit.kernel.AbstractAgent;
import madkit.kernel.AbstractAgent.ReturnCode;
import madkit.kernel.Activator;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Scheduler;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.2
 * @version 0.9
 * 
 */

public class BasicSchedulerTest extends JunitMadkit {

	@Test
	public void addingNullActivator() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				Scheduler s = new Scheduler();
				assertEquals(SUCCESS, launchAgent(s));
				try {
					Activator<AbstractAgent> a = new EmptyActivator(null, null, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator<AbstractAgent> a = new EmptyActivator(COMMUNITY, null, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator<AbstractAgent> a = new EmptyActivator(COMMUNITY, GROUP, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator<AbstractAgent> a = new EmptyActivator(null, GROUP, null);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
				try {
					Activator<AbstractAgent> a = new EmptyActivator(null, null, ROLE);
					s.addActivator(a);
					noExceptionFailure();
				} catch (NullPointerException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Test
	public void addingNullActivatorExceptionPrint() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				createDefaultCGR(this);
				Scheduler s = new Scheduler();
				assertEquals(SUCCESS, launchAgent(s));
				Activator<AbstractAgent> a = new EmptyActivator(null, null, null);
				s.addActivator(a);
			}
		}, ReturnCode.AGENT_CRASH);
	}

	@Test
	public void addingAndRemovingActivators() {
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, createGroup("public", "system", false, null));
				assertEquals(SUCCESS, requestRole("public", "system", "site", null));
				Scheduler s = new Scheduler() {
					public void live() {
						pause(10000);
					}
				};
				assertEquals(SUCCESS, launchAgent(s));
				ReturnCode code;
				// ///////////////////////// REQUEST ROLE ////////////////////////
				Activator<AbstractAgent> a = new EmptyActivator("public", "system", "site");
				s.addActivator(a);
				assertEquals(1, a.size());

				code = leaveRole("public", "system", "site");
				assertEquals(SUCCESS, code);
				assertEquals(0, a.size());

				assertEquals(ALREADY_GROUP, createGroup("public", "system", false, null));
				assertEquals(SUCCESS, requestRole("public", "system", "site", null));

				assertEquals(1, a.size());

				assertEquals(SUCCESS, leaveGroup("public", "system"));
				assertEquals(0, a.size());

				// Adding and removing while group does not exist
				s.removeActivator(a);
				assertEquals(0, a.size());
				s.addActivator(a);
				assertEquals(0, a.size());

				assertEquals(SUCCESS, createGroup("public", "system", false, null));
				assertEquals(SUCCESS, requestRole("public", "system", "site", null));
				AbstractAgent other = new AbstractAgent() {
					protected void activate() {
						assertEquals(SUCCESS, requestRole("public", "system", "site", null));
					}
				};
				assertEquals(SUCCESS, launchAgent(other));

				assertEquals(2, a.size());
				s.removeActivator(a);
				assertEquals(0, a.size());

				s.addActivator(a);
				assertEquals(2, a.size());

				assertEquals(SUCCESS, leaveGroup("public", "system"));
				assertEquals(1, a.size());
				assertEquals(SUCCESS, other.leaveGroup("public", "system"));
				assertEquals(0, a.size());

				assertEquals(SUCCESS, createGroup("public", "system", false, null));
				assertEquals(SUCCESS, requestRole("public", "system", "site", null));
				assertEquals(SUCCESS, other.requestRole("public", "system", "site", null));
				assertEquals(2, a.size());

				killAgent(s);
				assertEquals(0, a.size());
			}
		});
	}
	

}
