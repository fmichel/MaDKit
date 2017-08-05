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
package com.distrimind.madkit.api.abstractAgent;

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;

import java.util.logging.Level;

import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.testing.util.agent.DoItDuringLifeCycleAgent;
import com.distrimind.madkit.testing.util.agent.UnstopableAgent;

/**
 * 
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 *
 */
public class KillUnstoppableAgentTest extends JunitMadkit {

	@Test
	public void killUnstoppableInActivate() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true);
				unstopableAgent.setLogLevel(Level.ALL);
				startTimer();
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				stopTimer("launch time out ");
				assertEquals(TIMEOUT, killAgent(unstopableAgent, 1));
				assertAgentIsZombie(unstopableAgent);
			}
		});
	}

	@Test
	public void brutalKillUnstoppableInActivateAndLive() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true, true, false);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(TIMEOUT, killAgent(unstopableAgent, 0));
				JunitMadkit.pause(this, 100);
				assertAgentIsZombie(unstopableAgent);

			}
		});
	}

	@Test
	public void brutalKillUnsingInteruptedExceptionAtActivate() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {

				AbstractAgent unstopableAgent = new DoItDuringLifeCycleAgent(true, false, false) {

					@Override
					public void doIt() throws InterruptedException {
						waitNextMessage();
					}
				};
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(ReturnCode.SUCCESS, killAgent(unstopableAgent));
				JunitMadkit.pause(this, 100);
				assertAgentIsTerminated(unstopableAgent);

			}
		});
	}

	@Test
	public void brutalKillUnsingInteruptedExceptionAtLive() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {

				AbstractAgent unstopableAgent = new DoItDuringLifeCycleAgent(false, true, false) {

					@Override
					public void doIt() throws InterruptedException {
						waitNextMessage();
					}
				};
				assertEquals(ReturnCode.SUCCESS, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				JunitMadkit.pause(this, 100);
				assertEquals(ReturnCode.SUCCESS, killAgent(unstopableAgent, 2));
				JunitMadkit.pause(this, 100);
				assertAgentIsTerminated(unstopableAgent);

			}
		});
	}

	@Test
	public void brutalKillUnsingInteruptedExceptionAtEnd() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {

				AbstractAgent unstopableAgent = new DoItDuringLifeCycleAgent(false, false, true) {

					@Override
					public void doIt() throws InterruptedException {
						waitNextMessage();
					}
				};
				assertEquals(ReturnCode.SUCCESS, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				JunitMadkit.pause(this, 100);
				assertEquals(ReturnCode.ALREADY_KILLED, killAgent(unstopableAgent, 2));
				JunitMadkit.pause(this, 100);
				assertAgentIsTerminated(unstopableAgent);

			}
		});
	}

	@Test
	public void brutalKillUnstoppableInActivateAndLiveAndEnd() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true, true, true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				if (logger != null)
					logger.info(unstopableAgent.getState().toString());
				assertEquals(TIMEOUT, killAgent(unstopableAgent, 1));
				assertAgentIsZombie(unstopableAgent);
				JunitMadkit.pause(this, 1000);
			}
		});
	}

	@Test
	public void brutalKillUnstoppableUsingSelfRef() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent unstopableAgent = new UnstopableAgent(true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				assertEquals(TIMEOUT, unstopableAgent.killAgent(unstopableAgent, 1));
				assertAgentIsZombie(unstopableAgent);
				JunitMadkit.pause(this, 1000);
				unstopableAgent = new UnstopableAgent(true, false, true);
				assertEquals(TIMEOUT, launchAgent(unstopableAgent, 1));
				assertEquals(TIMEOUT, unstopableAgent.killAgent(unstopableAgent, 1));
				assertAgentIsZombie(unstopableAgent);
				JunitMadkit.pause(this, 1000);
			}
		});
	}

}
