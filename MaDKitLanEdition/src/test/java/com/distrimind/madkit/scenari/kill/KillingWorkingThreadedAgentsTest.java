/*
 * MadKitLanEdition (created by Jason MAHDJOUB (jason.mahdjoub@distri-mind.fr)) Copyright (c)
 * 2015 is a fork of MadKit and MadKitGroupExtension. 
 * 
 * Copyright or © or Copr. Jason Mahdjoub, Fabien Michel, Olivier Gutknecht, Jacques Ferber (1997)
 * 
 * jason.mahdjoub@distri-mind.fr
 * fmichel@lirmm.fr
 * olg@no-distance.net
 * ferber@lirmm.fr
 * 
 * This software is a computer program whose purpose is to
 * provide a lightweight Java library for designing and simulating Multi-Agent Systems (MAS).
 * This software is governed by the CeCILL-C license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-C
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * 
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-C license and that you accept its terms.
 */
package com.distrimind.madkit.scenari.kill;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.ALREADY_KILLED;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.logging.Level;

import org.junit.Assert;
import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.Agent;
import com.distrimind.madkit.kernel.JUnitAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.testing.util.agent.UnstopableAgent;
import com.distrimind.madkit.testing.util.agent.WorkingAgent;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.10
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 */

public class KillingWorkingThreadedAgentsTest extends JunitMadkit {

	@Test
	public void killUnstopable() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent();
				unstopableAgent.setLogLevel(Level.FINER);
				startTimer();
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				stopTimer("launch time out ");
				assertEquals(TIMEOUT, killAgent(unstopableAgent, 1));
				assertAgentIsZombie(unstopableAgent);
			}
		});
	}

	@Test
	public void brutalKills() {// TODO brutal kill with to < 0
		addMadkitArgs("--agentLogLevel", "ALL");
		addMadkitArgs("--kernelLogLevel", "FINEST");
		launchTest(new AbstractAgent() {
			@Override
			public void activate() {
				Agent a;
				a = new WorkingAgent(true, false, false);
				ReturnCode r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 1);
				JunitMadkit.pause(this, 1000);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(false, true, false);
				assertEquals(SUCCESS, launchAgent(a));
				JunitMadkit.pause(this, 3000);
				r = killAgent(a, 0);
				assertTrue(r == ALREADY_KILLED || r == TIMEOUT);
				JunitMadkit.pause(this, 100);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(false, false, true);
				assertEquals(SUCCESS, launchAgent(a));
				JunitMadkit.pause(this, 3000);
				r = killAgent(a, 0);
				assertTrue(ALREADY_KILLED == r || r == TIMEOUT);
				JunitMadkit.pause(this, 100);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(true, false, true);
				launchAgent(a, 2);
				r = killAgent(a, 0);
				assertTrue(r == TIMEOUT || r == ReturnCode.KILLING_ALREADY_IN_PROGRESS);
				assertAgentIsZombie(a);
				JunitMadkit.pause(this, 4200);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillOnEnd() {
		addMadkitArgs("--agentLogLevel", "ALL");
		addMadkitArgs("--kernelLogLevel", "FINEST");
		launchTest(new AbstractAgent() {
			@Override
			public void activate() {
				Agent a;
				a = new WorkingAgent(false, false, true);
				assertEquals(SUCCESS, launchAgent(a));
				JunitMadkit.pause(this, 200);
				killAgent(a, 0);
				JunitMadkit.pause(this, 3200);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillOnActivate() {
		addMadkitArgs("--agentLogLevel", "ALL");
		addMadkitArgs("--kernelLogLevel", "FINEST");
		launchTest(new AbstractAgent() {
			@Override
			public void activate() {
				Agent a;
				a = new WorkingAgent(true, false, false);
				ReturnCode r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 2);
				assertAgentIsTerminated(a);
				a = new WorkingAgent(true, true, false);
				r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 1);

				JunitMadkit.pause(this, 5500);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillOnLive() {
		addMadkitArgs("--agentLogLevel", "ALL");
		addMadkitArgs("--kernelLogLevel", "FINEST");
		launchTest(new JUnitAgent() {
			@Override
			public void activate() {
				Agent a;
				a = new WorkingAgent(false, true, false);
				successOnLaunch(a);
				JunitMadkit.pause(this, 100);
				killAgent(a, 0);
				assertAgentIsZombie(a);
				JunitMadkit.pause(this, 3000);
				assertAgentIsTerminated(a);
			}
		});
	}

	@Test
	public void brutalKillOnLiveWTO() {
		addMadkitArgs("--agentLogLevel", "ALL");
		addMadkitArgs("--kernelLogLevel", "FINEST");
		launchTest(new JUnitAgent() {
			@Override
			public void activate() {
				Agent a;
				a = new WorkingAgent(false, true, true);
				successOnLaunch(a);
				JunitMadkit.pause(this, 10);
				killAgent(a, 1);
				assertAgentIsZombie(a);
				JunitMadkit.pause(this, 5500);
				assertAgentIsTerminated(a);
				a = new WorkingAgent(false, true, false);
				successOnLaunch(a);
				JunitMadkit.pause(this, 10);
				killAgent(a, 0);
				JunitMadkit.pause(this, 100);
				assertAgentIsZombie(a);
				JunitMadkit.pause(this, 3200);
				assertAgentIsTerminated(a);

			}
		});
	}

	@Test
	public void brutalKillonAll() {// TODO brutal kill with to < 0
		launchTest(new AbstractAgent() {
			@Override
			public void activate() {
				Agent a;
				a = new WorkingAgent(true, true, true);
				launchAgent(a, 2);
				ReturnCode r = killAgent(a, 0);
				assertTrue(r.toString(), TIMEOUT == r || r == SUCCESS);
				assertAgentIsZombie(a);
				JunitMadkit.pause(this, 8500);
				assertAgentIsTerminated(a);
			}
		}, true);
	}

	@Test
	public void brutalKillonAllWTO() {
		launchTest(new AbstractAgent() {
			@Override
			public void activate() {
				Agent a;
				a = new WorkingAgent(true, true, true);
				ReturnCode r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				r = killAgent(a, 1);
				Assert.assertEquals(TIMEOUT, r);
				assertAgentIsZombie(a);
				JunitMadkit.pause(this, 10000);
				assertAgentIsTerminated(a);
			}
		}, true);
	}

	@Test
	public void normalKills() {// TODO more cases
		launchTest(new AbstractAgent() {
			@Override
			public void activate() throws InterruptedException {
				super.activate();
				Agent a;
				a = new WorkingAgent(true, false, false);
				ReturnCode r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a);
				JunitMadkit.pause(this, 200);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(false, true, false);
				assertEquals(SUCCESS, launchAgent(a));
				killAgent(a);
				assertAgentIsTerminated(a);

				a = new WorkingAgent(false, false, true);
				assertEquals(SUCCESS, launchAgent(a));
				JunitMadkit.pause(this, 100);
				killAgent(a, 3);
				assertEquals(State.TERMINATED, a.getState());

				a = new WorkingAgent(true, false, true);
				r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				killAgent(a, 2);
				assertTrue(State.ZOMBIE == a.getState() || State.ENDING == a.getState());

				a = new WorkingAgent(true, true, true);
				r = launchAgent(a, 1);
				assertTrue(TIMEOUT == r || r == SUCCESS);
				while (a.getState().compareTo(State.ACTIVATING) < 0)
					pause(100);
				killAgent(a, 1);
				assertTrue(a.getState().toString(), State.ZOMBIE == a.getState() || State.ENDING == a.getState());
			}
		});
	}
}
