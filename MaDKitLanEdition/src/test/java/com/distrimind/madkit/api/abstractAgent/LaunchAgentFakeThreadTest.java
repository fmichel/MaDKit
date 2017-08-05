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

import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.AGENT_CRASH;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.ALREADY_LAUNCHED;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.SUCCESS;
import static com.distrimind.madkit.kernel.AbstractAgent.ReturnCode.TIMEOUT;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.testing.util.agent.SelfLaunchAgentFakeThread;
import com.distrimind.madkit.testing.util.agent.SimpleAgentFakeThread;

/**
 * 
 * @author Jason Mahdjoub
 * @version 1.0
 * @since MadkitLanEdition 1.0
 */
public class LaunchAgentFakeThreadTest extends JunitMadkit {
	final AbstractAgent target = new SimpleAgentFakeThread() {
		@Override
		protected void activate() throws InterruptedException {
			super.activate();
			assertEquals(SUCCESS, createGroup(GROUP));
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
			assertEquals(ALREADY_LAUNCHED, launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new SimpleAgentFakeThread() {
		@Override
		protected void activate() throws InterruptedException {
			super.activate();
			JunitMadkit.pause(this, 2000);

		}

	};

	final AbstractAgent faulty = new SimpleAgentFakeThread() {
		@SuppressWarnings("null")
		@Override
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void returnSuccessAndAlreadyLaunch() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(ALREADY_LAUNCHED, launchAgent(target));
				assertEquals(ALREADY_LAUNCHED, launchAgent(this));
				assertEquals(ReturnCode.SUCCESS, killAgent(target));
			}
		});
	}

	@Test
	public void returnTimeOut() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
				assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
				assertEquals(ReturnCode.SUCCESS, killAgent(timeOutAgent));
			}
		});
	}

	@Test
	public void returnAleradyLaunch() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
				assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
				assertEquals(ReturnCode.SUCCESS, killAgent(timeOutAgent));
			}
		});
	}

	@Test
	public void killLauncher() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				AbstractAgent a;
				launchAgent(a = new AbstractAgent() {
					@Override
					protected void activate() {
						assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
					}
				}, 1);
				killAgent(a);
				assertAgentIsTerminated(a);
				assertEquals(ReturnCode.SUCCESS, killAgent(timeOutAgent));
			}
		}, true);
		pause(null, 1000);
	}

	@Test
	public void returnAgentCrash() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(faulty, 1));
				assertEquals(ALREADY_LAUNCHED, launchAgent(faulty));
			}
		});
	}

	@Test
	public void selfLaunching() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				SelfLaunchAgentFakeThread a = new SelfLaunchAgentFakeThread(true);
				assertEquals(SUCCESS, launchAgent(a, 1));
				a = new SelfLaunchAgentFakeThread(false, true);
				assertEquals(SUCCESS, launchAgent(a, 1));
				a = new SelfLaunchAgentFakeThread(false, false, true);
				assertEquals(SUCCESS, launchAgent(a, 1));
			}
		});
	}

	@Test
	public void nullArgs() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(TIMEOUT, launchAgent(new AbstractAgent(), -1, true));
			}
		});
	}

}
