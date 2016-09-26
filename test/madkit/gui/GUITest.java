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
package madkit.gui;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.swing.JFrame;

import madkit.action.AgentAction;
import madkit.kernel.AbstractAgent;
import madkit.kernel.Agent;
import madkit.kernel.JunitMadkit;
import madkit.kernel.Message;
import madkit.testing.util.agent.AlwaysInCGRNormalAA;

import org.junit.Test;

/**
 * @author Fabien Michel
 * @since MaDKit 5.0.0.9
 * @version 0.9
 * 
 */

public class GUITest extends JunitMadkit {

	@Test
	public void hasGUITest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent a = new AbstractAgent();
				assertEquals(ReturnCode.SUCCESS, launchAgent(a, true));
				assertTrue(a.hasGUI());
			}
		});
	}

	public void kill() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent a = new AbstractAgent();
				launchAgent(a, 0, true);
					for (int i = 0; i < 100; i++) {
						killAgent(a);
					}
			}
		});
	}

	@Test
	public void setupFrameTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(ReturnCode.SUCCESS, launchAgent(new AbstractAgent(){
					private boolean ok = false;
					private JFrame f;
					@Override
					public void setupFrame(JFrame frame) {
						ok = true;
						f = frame;
					}
					@Override
					protected void activate() {
						assertTrue(ok);
						f.dispose();
					}
				}, true));
			}
		});
	}

	@Test
	public void noAAMessageTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(ReturnCode.SUCCESS, launchAgent(new AbstractAgent(){
					@Override
					public void setupFrame(JFrame frame) {
						assertNotNull(frame);
					}
					@Override
					protected void activate() {
						Message m = nextMessage();
						System.err.println(m);
					}
				}, true));
			}
		});
	}

	@Test
	public void launchAgentByGUITest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent a = launchAgent(AlwaysInCGRNormalAA.class.getName());
				assertEquals(1, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
				AgentAction.LAUNCH_AGENT.getActionFor(a,a.getClass().getName(),true).actionPerformed(null);
				assertEquals(2, getAgentsWithRole(COMMUNITY, GROUP, ROLE).size());
			}
		});
	}


	@Test
	public void killAgentByGUITest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent a = launchAgent(AlwaysInCGRNormalAA.class.getName());
				AgentAction.KILL_AGENT.getActionFor(a,a).actionPerformed(null);
				assertFalse(a.isAlive());
			}
		});
	}

	@Test
	public void noAgentMessageTest() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(ReturnCode.SUCCESS, launchAgent(new Agent(){
					@Override
					public void setupFrame(JFrame frame) {
						assertNotNull(frame);
					try {
							Thread.sleep(10000);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					@Override
					protected void activate() {
						Message m = nextMessage();
						System.err.println(m);
					}
				}, true));
			}
		});
	}

}