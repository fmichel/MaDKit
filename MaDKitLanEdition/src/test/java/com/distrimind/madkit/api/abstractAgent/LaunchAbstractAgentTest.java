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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.distrimind.madkit.kernel.AbstractAgent;
import com.distrimind.madkit.kernel.JunitMadkit;
import com.distrimind.madkit.testing.util.agent.BuggedConstructorAgent;
import com.distrimind.madkit.testing.util.agent.BuggedFrameAgent;
import com.distrimind.madkit.testing.util.agent.SelfLaunchAA;

/**
 * @author Fabien Michel
 * @author Jason Mahdjoub
 * @since MaDKit 5.0.0.7
 * @since MadkitLanEdition 1.0
 * @version 1.0
 * 
 * 
 */

public class LaunchAbstractAgentTest extends JunitMadkit {

	final AbstractAgent target = new AbstractAgent() {
		@Override
		protected void activate() {
			assertEquals(SUCCESS, createGroup(GROUP));
			assertEquals(SUCCESS, requestRole(GROUP, ROLE));
			assertEquals(ALREADY_LAUNCHED, launchAgent(this));
		}
	};

	final AbstractAgent timeOutAgent = new AbstractAgent() {
		@Override
		protected void activate() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	};

	final AbstractAgent faulty = new AbstractAgent() {
		@SuppressWarnings("null")
		@Override
		protected void activate() {
			Object o = null;
			o.toString();
		}
	};

	@Test
	public void returnSuccessAndAlreadyNoGUI() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				// assertNull(target.getGUIComponent());
				assertEquals(ALREADY_LAUNCHED, launchAgent(target));
			}
		});
	}

	@Test
	public void launchBuggedFrameAgent() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new BuggedFrameAgent(), true);
			}
		});
	}

	@Test
	public void launchBuggedConstructorAgent() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				launchAgent(new BuggedConstructorAgent());
			}
		}, AGENT_CRASH);
	}

	@Test
	public void nullArgs() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent((AbstractAgent) null));
				noExceptionFailure();
			}
		}, AGENT_CRASH);
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent((AbstractAgent) null, true));
				noExceptionFailure();
			}
		}, AGENT_CRASH);
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent((AbstractAgent) null, 1));
				noExceptionFailure();
			}
		}, AGENT_CRASH);
		launchTest(new AbstractAgent() {
			protected void activate() {
				assertEquals(SUCCESS, launchAgent((AbstractAgent) null, 1, true));
				noExceptionFailure();
			}
		}, AGENT_CRASH);
	}

	@Test
	public void returnSuccessAndAlreadyLaunchWithGUI() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target, true));
				assertEquals(ALREADY_LAUNCHED, launchAgent(target, true));
				// assertNotNull(target.getGUIComponent());
			}
		});
	}

	@Test
	public void returnTimeOut() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(TIMEOUT, launchAgent(timeOutAgent, 1));
				assertEquals(TIMEOUT, launchAgent(new AbstractAgent(), 0));
				assertEquals(TIMEOUT, launchAgent(new AbstractAgent(), -1));
				assertEquals(ALREADY_LAUNCHED, launchAgent(timeOutAgent));
			}
		});
	}

	@Test
	public void massLaunch() {
		addMadkitArgs("--" + "agentLogLevel", "OFF");
		launchTest(new AbstractAgent() {
			final int number = 1000;

			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(JunitMadkit.GROUP));
				startTimer();
				for (int i = 0; i < number; i++) {
					assertEquals(SUCCESS, launchAgent(new AbstractAgent()));
				}
				stopTimer("launch time ");
				startTimer();
				for (int i = 0; i < number; i++) {
					ReturnCode r = launchAgent(new AbstractAgent(), 0);
					assertTrue(TIMEOUT == r || SUCCESS == r);
				}
				stopTimer("launch time ");
			}
		});
	}

	@Test
	public void massLaunchWithGUI() {
		addMadkitArgs("--" + "agentLogLevel", "OFF");
		launchTest(new AbstractAgent() {
			final int number = 10;

			@Override
			protected void activate() {
				assertEquals(SUCCESS, createGroup(JunitMadkit.GROUP));
				startTimer();
				for (int i = 0; i < number; i++) {
					assertEquals(SUCCESS, launchAgent(new AbstractAgent(), true));
				}
				stopTimer("launch time ");
				startTimer();
				for (int i = 0; i < number; i++) {
					ReturnCode r = launchAgent(new AbstractAgent(), 0, true);
					assertTrue(TIMEOUT == r || SUCCESS == r);
				}
				stopTimer("launch time ");
			}
		});
	}

	@Test
	public void returnAleradyLaunch() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(SUCCESS, launchAgent(target));
				assertEquals(ALREADY_LAUNCHED, launchAgent(target));

				ReturnCode r = launchAgent(timeOutAgent, 0, true);
				assertTrue(TIMEOUT == r || SUCCESS == r);
				r = launchAgent(timeOutAgent, true);
				assertTrue(ALREADY_LAUNCHED == r || SUCCESS == r);
			}
		});
	}

	@Test
	public void returnAgentCrash() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				assertEquals(AGENT_CRASH, launchAgent(faulty));
				assertEquals(ALREADY_LAUNCHED, launchAgent(faulty));
			}
		});
	}

	@Test
	public void SelfLaunching() {
		addMadkitArgs("--kernelLogLevel", "ALL");
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				SelfLaunchAA a = new SelfLaunchAA(true);
				assertEquals(SUCCESS, launchAgent(a, 1));
				a = new SelfLaunchAA(false, true);
				assertEquals(SUCCESS, launchAgent(a, 1));
			}
		});
	}

	@Test
	public void chainLaunching() {
		launchTest(new AbstractAgent() {
			@Override
			protected void activate() {
				for (int i = 0; i < 100; i++) {
					launchAgent(new AbstractAgent() {
						@Override
						protected void activate() {
							launchAgent(new AbstractAgent() {
								@Override
								protected void activate() {
									launchAgent(new AbstractAgent());
								}
							});
						}
					});
				}
			}
		});
	}
}
